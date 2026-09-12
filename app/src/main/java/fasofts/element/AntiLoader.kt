package fasofts.element

import java.io.File
import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object AntiLoader {

    private const val KEY_PART_COUNT = 8

    fun runChecks(ctx: Context): Boolean {
        return try {

            // CHAVE
            val parts = mutableListOf<ByteArray>()
            for (i in 0 until KEY_PART_COUNT) {
                try {
                    val clsName = "fasofts.element.sysdata.KeyPart$i"
                    val cls = Class.forName(clsName)
                    val m = cls.getMethod("getKPart")
                    val b64 = m.invoke(null) as String
                    parts.add(Base64.decode(b64, Base64.DEFAULT))
                } catch (_: Throwable) {
                    // silencioso
                }
            }

            val merged = parts.flatMap { it.asIterable() }.toByteArray()
            val keyBytes = if (merged.size >= 32) merged.copyOf(32) else ByteArray(32) { merged.getOrNull(it) ?: 0 }
            val key = SecretKeySpec(keyBytes, "AES")

            val ins = try { ctx.assets.open(".anti_data/cache_res_v3.dat") } catch (_: Exception) { null }

            val hashes = if (ins != null) {
                val iv = ByteArray(12)
                ins.read(iv)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
                val plain = cipher.doFinal(ins.readBytes())
                String(plain).lines().map { it.trim() }.filter { it.isNotEmpty() }
            } else emptyList()

            val installed = ctx.packageManager.getInstalledPackages(0).map { it.packageName }

            if (hashes.any { h -> installed.any { sha256(it) == h } }) {
                false
            } else if (isDeviceEmulator() || isRooted()) {
                false
            } else {
                true
            }

        } catch (_: Throwable) {
            true
        }
    }

    private fun sha256(s: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun isDeviceEmulator(): Boolean {
        val fp = android.os.Build.FINGERPRINT
        val model = android.os.Build.MODEL
        return fp.contains("generic") || fp.contains("emulator")
                || model.contains("Emulator") || model.contains("Android SDK built for x86")
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf("/system/xbin/su", "/system/bin/su", "/sbin/su")
        return paths.any { File(it).exists() }
    }
}