package fasofts.element.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object Security {

    // Package esperado (ApplicationId)
    private const val EXPECTED_PKG = "fasofts.element"

    // SHA-256 esperado (formato HEX com dois dígitos e ':' entre bytes, uppercase)
    // Valor provido:
    private const val EXPECTED_SHA256 = "7B:5B:6D:A9:54:59:D9:9A:7C:B5:F4:0C:0B:3D:A6:DF:01:3A:5F:A8:9F:CA:3F:F0:77:AF:64:E5:71:6C:3C:D0"

    @Throws(SecurityException::class)
    fun validateSignature(context: Context) {
        // 1) Verifica package
        val pkg = context.packageName
        if (pkg != EXPECTED_PKG) {
            throw SecurityException("PACOTE ALTERADO: esperado=$EXPECTED_PKG, atual=$pkg")
        }

        // 2) Verifica SHA-256 da assinatura
        val sha = getSignatureSHA256(context)
        if (!sha.equals(EXPECTED_SHA256, ignoreCase = false)) {
            throw SecurityException("ASSINATURA ALTERADA: esperado=$EXPECTED_SHA256, atual=$sha")
        }
    }

    private fun toHexColonUpper(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append(String.format("%02X", bytes[i]))
            if (i != bytes.size - 1) sb.append(':')
        }
        return sb.toString()
    }

    private fun getSignatureSHA256(context: Context): String {
        try {
            val pm = context.packageManager
            // API 28+ use signingInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signers = pi.signingInfo.apkContentsSigners
                if (signers != null && signers.isNotEmpty()) {
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val cert = certFactory.generateCertificate(signers[0].toByteArray().inputStream()) as X509Certificate
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(cert.encoded)
                    return toHexColonUpper(digest)
                }
            } else {
                // Fallback pré-P
                val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                val sigs = pi.signatures
                if (sigs != null && sigs.isNotEmpty()) {
                    val certFactory = CertificateFactory.getInstance("X.509")
                    val cert = certFactory.generateCertificate(sigs[0].toByteArray().inputStream()) as X509Certificate
                    val md = MessageDigest.getInstance("SHA-256")
                    val digest = md.digest(cert.encoded)
                    return toHexColonUpper(digest)
                }
            }
        } catch (e: Exception) {
            // se ocorrer qualquer erro -> tratar como adulteração
            return ""
        }
        return ""
    }
}