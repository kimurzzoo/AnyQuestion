package com.example.anyquestion.util

import org.springframework.beans.factory.annotation.Value
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class OtherUtils {
    companion object
    {
        @Value("\${toss.orderid.key}")
        val secretkey = ""

        fun genOrderId(userEmail : String) : String
        {
            val keySpec = SecretKeySpec(secretkey.toByteArray(), "AES")    /// 키
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")     //싸이퍼
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)       // 암호화/복호화 모드

            val beforetext = userEmail + System.currentTimeMillis().toString()

            val ciphertext = cipher.doFinal(beforetext.toByteArray())
            val encodedByte = Base64.getEncoder().encode(ciphertext)
            return String(encodedByte)
        }

        fun decodeOrderId(orderId : String) : String
        {
            val keySpec = SecretKeySpec(secretkey.toByteArray(), "AES")
            var decodedByte: ByteArray = Base64.getDecoder().decode(orderId)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val output = cipher.doFinal(decodedByte)

            return String(output)
        }
    }
}