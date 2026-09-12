package fasofts.element.security

import android.app.Application
import android.content.pm.PackageManager
import android.os.Process
import fasofts.element.BuildConfig
import java.security.MessageDigest

object TamperGuard {

    private fun sha256Hex(bytes: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(bytes)
        val sb = StringBuilder(d.size * 2)
        for (b in d) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    private fun currentCertSha256(app: Application): String {
        val pm = app.packageManager
        val pi = pm.getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val cert = pi.signingInfo.apkContentsSigners.first().toByteArray()
        return sha256Hex(cert)
    }

    private fun ensureClassesExist() {
        val list = BuildConfig.REQUIRED_CLASSES_CSV
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        for (fqcn in list) {
            // Falha se a classe não existir ou se nome foi ofuscado
            val cls = Class.forName(fqcn)
            // Opcional: garante que é uma Activity
            if (!android.app.Activity::class.java.isAssignableFrom(cls)) {
                throw SecurityException("Classe $fqcn não é Activity")
            }
        }
    }

    fun validateOrDie(app: Application) {
        // 1) Verifica a assinatura do APK (detecta repackage/adulteração)
        val expected = BuildConfig.EXPECTED_CERT_SHA256
        if (expected.isNotEmpty()) {
            val actual = currentCertSha256(app)
            if (!actual.equals(expected, ignoreCase = true)) {
                // Mata o app antes de qualquer UI
                Process.killProcess(Process.myPid())
                throw SecurityException("Assinatura inválida")
            }
        }

        // 2) Verifica existência das classes exigidas
        ensureClassesExist()
    }
}
