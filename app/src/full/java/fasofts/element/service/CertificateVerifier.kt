package fasofts.element.service;

import android.content.pm.PackageManager
import android.util.Base64
import fasofts.element.BuildConfig
import java.security.MessageDigest

object CertificateVerifier {

    fun checkCertificate(pm: PackageManager, packageName: String): Boolean {
        val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                ?: return false

        val cert = info.signingInfo.apkContentsSigners.firstOrNull() ?: return false
        val sha = sha256(cert.toByteArray())

        return sha.equals(BuildConfig.EXPECTED_CERT_SHA256, ignoreCase = true)
    }

    private fun sha256(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(data)
            .joinToString(":") { "%02X".format(it) }
    }
}