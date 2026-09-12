package fasofts.element.service;

import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

class RSAKeyless {

    private val keyPair: KeyPair by lazy {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        generator.generateKeyPair()
    }

    fun getPublicKey(): PublicKey = keyPair.public

    fun getPrivateKey(): PrivateKey = keyPair.private

    fun encrypt(data: ByteArray): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
        val encrypted = cipher.doFinal(data)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(base64: String): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
        val decoded = Base64.decode(base64, Base64.NO_WRAP)
        return cipher.doFinal(decoded)
    }
}