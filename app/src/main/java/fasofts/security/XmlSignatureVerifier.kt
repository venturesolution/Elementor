package fasofts.element.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.json.JSONObject
import java.io.InputStream
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.util.Base64

/**
 * Verificador de assinaturas dos XML em assets/protected.
 * - valida cada assinatura SHA256withRSA presente em signatures.json
 * - valida fingerprint SHA-256 do certificado instalado contra BuildConfig.EXPECTED_CERT_SHA256 (se presente)
 * - em caso de falha, lançar RuntimeException para interromper a execução.
 */
object XmlSignatureVerifier {

    private fun loadSignaturesJson(context: Context): JSONObject? {
        return try {
            context.assets.open("protected/signatures.json").bufferedReader().use { r ->
                JSONObject(r.readText())
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun computeSha256Hex(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    // Recupera a chave pública do certificado que assinou o APK instalado
    private fun getInstalledApkSigningPublicKey(context: Context): PublicKey? {
        try {
            val pm = context.packageManager
            val pkgName = context.packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = pi.signingInfo ?: return null
                val certs = if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                if (certs != null && certs.isNotEmpty()) {
                    val certBytes = certs[0].toByteArray()
                    val cf = CertificateFactory.getInstance("X.509")
                    val cert = cf.generateCertificate(certBytes.inputStream())
                    return cert.publicKey
                }
            } else {
                @Suppress("DEPRECATION")
                val pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
                val sigs = pi.signatures
                if (sigs != null && sigs.isNotEmpty()) {
                    val certBytes = sigs[0].toByteArray()
                    val cf = CertificateFactory.getInstance("X.509")
                    val cert = cf.generateCertificate(certBytes.inputStream())
                    return cert.publicKey
                }
            }
        } catch (e: Exception) {
            // falha ao recuperar cert do APK
        }
        return null
    }

    // Recupera SHA256 hex do certificado instalado (para comparação com BuildConfig)
    private fun getInstalledApkCertSha256Hex(context: Context): String? {
        try {
            val pm = context.packageManager
            val pkgName = context.packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = pi.signingInfo ?: return null
                val certs = if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
                if (certs != null && certs.isNotEmpty()) {
                    val certBytes = certs[0].toByteArray()
                    val cf = CertificateFactory.getInstance("X.509")
                    val cert = cf.generateCertificate(certBytes.inputStream())
                    return computeSha256Hex(cert.encoded)
                }
            } else {
                @Suppress("DEPRECATION")
                val pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
                val sigs = pi.signatures
                if (sigs != null && sigs.isNotEmpty()) {
                    val certBytes = sigs[0].toByteArray()
                    val cf = CertificateFactory.getInstance("X.509")
                    val cert = cf.generateCertificate(certBytes.inputStream())
                    return computeSha256Hex(cert.encoded)
                }
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun verifySignature(publicKey: PublicKey, data: ByteArray, signatureB64: String): Boolean {
        return try {
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(data)
            val sigBytes = Base64.getDecoder().decode(signatureB64)
            sig.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica todas as assinaturas e retorna lista de problemas (vazia = OK).
     */
    fun verifyAllProtectedXmls(context: Context): List<String> {
        val problems = mutableListOf<String>()
        val json = loadSignaturesJson(context) ?: run {
            problems.add("signatures.json não encontrado em assets/protected")
            return problems
        }
        val publicKey = getInstalledApkSigningPublicKey(context) ?: run {
            problems.add("Não foi possível recuperar chave pública do certificado instalado")
            return problems
        }

        val keys = json.keys()
        while (keys.hasNext()) {
            val rel = keys.next()
            val signatureB64 = json.getString(rel)
            val assetPath = rel.replaceFirst("^src/main/", "")
            try {
                val stream: InputStream = context.assets.open(assetPath)
                val bytes = stream.readBytes()
                stream.close()
                val ok = verifySignature(publicKey, bytes, signatureB64)
                if (!ok) problems.add("Falha na assinatura: $assetPath")
            } catch (e: Exception) {
                problems.add("Arquivo protegido não encontrado no assets: $assetPath")
            }
        }
        return problems
    }

    /**
     * Verifica fingerprint do certificado instalado contra BuildConfig.EXPECTED_CERT_SHA256 (se BuildConfig define).
     * Retorna null se BuildConfig não existir ou falhar. Caso contrário retorna SHA256 hex do certificado.
     */
    private fun checkExpectedCertFingerprint(context: Context): Pair<Boolean, String?> {
        return try {
            val pkg = context.packageName
            val field = Class.forName("$pkg.BuildConfig").getField("EXPECTED_CERT_SHA256")
            val expected = field.get(null) as? String ?: ""
            if (expected.isEmpty()) return Pair(true, null) // nada a checar
            val installed = getInstalledApkCertSha256Hex(context) ?: return Pair(false, null)
            Pair(expected.equals(installed, ignoreCase = true), installed)
        } catch (e: Exception) {
            Pair(true, null)
        }
    }

    /**
     * Verifica tudo e lança RuntimeException em caso de qualquer problema de integridade.
     * Use no Application.onCreate() antes da inicialização sensível.
     */
    fun verifyAndEnforce(context: Context) {
        // 1) checar fingerprint (opcional mas recomendado)
        val (fingerprintOk, installedSha) = checkExpectedCertFingerprint(context)
        if (!fingerprintOk) {
            val inst = installedSha ?: "desconhecido"
            throw RuntimeException("CERT FINGERPRINT MISMATCH. Instalado=$inst")
        }

        // 2) checar assinaturas dos XML protegidos
        val problems = verifyAllProtectedXmls(context)
        if (problems.isNotEmpty()) {
            val joined = problems.joinToString("; ")
            throw RuntimeException("XML integrity check failed: $joined")
        }
        // passa
    }
}