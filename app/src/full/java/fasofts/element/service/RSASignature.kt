package fasofts.element.service;

import android.util.Base64
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object RSASignature {

    // SUA CHAVE PÚBLICA EM BASE64 – APENAS PARA VERIFICAÇÃO
    private const val PUBLIC_KEY_B64 =
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwQp4l3E7xEEsx7H5ABjl" +
"jPS8f0iK6B8E6H0zuEzmm2R3RKn1YFj2oYgOnGcb0uH/0M6pnb3pJlYqB4Pi3o8v" +
"gJ7RXBVH1kjK7l2S9n1piC8G0QGfWWCg/Is7lX+hBWbM6XUQ7eUanKB1gwzXMWyT" +
"olqz3o3lGsZw3X6R7xbZV/ZRpsV2uV7gXX6M8BI9l2bpcq6pg2fPVk7L3v98B6nM" +
"B0bq4KeVWgN2ZJ8E2fxc9JZX1dd9cv7H5uMqJ6cC2+nfnVfVEpOG6UEG5xwT5SKl" +
"q1zooX1BkEAmNVknA1eJxsTIGjrM0qlcKSe9sEeHX+Y30PTLxqNOyBYJYpYCbMvT" +
"vwIDAQAB";

    private val publicKey by lazy {
        val decoded = Base64.decode(PUBLIC_KEY_B64, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(decoded)
        KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    fun verify(data: ByteArray, signatureB64: String): Boolean {
        return try {
            val sigBytes = Base64.decode(signatureB64, Base64.DEFAULT)
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(data)
            sig.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }
}