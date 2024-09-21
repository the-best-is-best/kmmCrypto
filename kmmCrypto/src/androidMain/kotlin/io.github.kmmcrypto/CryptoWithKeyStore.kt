package io.github.kmmcrypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.M)
internal class CryptoKeyStore {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    // Encrypt and save data using AES encryption
    fun encryptAndSave(key: String, group: String, data: String): ByteArray {
        val keyPair = getKeyPair()

        // Generate AES key
        val aesKey = generateAESKey()
        println(
            "Generated AES Key: ${
                Base64.encodeToString(
                    aesKey.encoded,
                    Base64.DEFAULT
                )
            }"
        ) // Log the AES key

        // Encrypt the data with AES
        val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val iv = aesCipher.iv // Initialization vector for AES
        val encryptedData = aesCipher.doFinal(data.toByteArray())

        // Encrypt the AES key with the RSA public key
        val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
        val encryptedAESKey = rsaCipher.doFinal(aesKey.encoded)

        // Log sizes for debugging
        println("Encrypted AES Key Size: ${encryptedAESKey.size}")

        // Get the directory path for the group folder
        val groupDir = File(AndroidKMMCrypto.activity.filesDir, group)
        if (!groupDir.exists()) {
            groupDir.mkdirs()
        }

        // Create the file in the specified group folder
        val file = File(groupDir, key)

        // Write the encrypted AES key, IV, and encrypted data to the file
        file.outputStream().use { fos ->
            // Write encrypted AES key size (4 bytes)
            val aesKeySize = encryptedAESKey.size
            fos.write(aesKeySize shr 24 and 0xFF)
            fos.write(aesKeySize shr 16 and 0xFF)
            fos.write(aesKeySize shr 8 and 0xFF)
            fos.write(aesKeySize and 0xFF)
            fos.write(encryptedAESKey) // Write encrypted AES key

            // Write IV size (1 byte)
            fos.write(iv.size)
            fos.write(iv) // Write IV

            // Write encrypted data size (4 bytes)
            val encryptedDataSize = encryptedData.size
            fos.write(encryptedDataSize shr 24 and 0xFF)
            fos.write(encryptedDataSize shr 16 and 0xFF)
            fos.write(encryptedDataSize shr 8 and 0xFF)
            fos.write(encryptedDataSize and 0xFF)
            fos.write(encryptedData) // Write encrypted data
        }

        return encryptedData
    }


    // Retrieve and decrypt data using AES decryption
    fun retrieveAndDecrypt(key: String, group: String): String? {
        val keyPair = getKeyPair()

        // Construct the file path using the group directory and key as the filename
        val file = File(AndroidKMMCrypto.activity.filesDir, "$group/$key")

        // Check if the file exists
        if (!file.exists()) {
            return null
        }

        // Read the encrypted data from the file
        val fileBytes = file.readBytes()
        var offset = 0

        // Read the size of the encrypted AES key (4 bytes)
        val aesKeyLength = (fileBytes[offset].toInt() shl 24) or
                (fileBytes[offset + 1].toInt() shl 16) or
                (fileBytes[offset + 2].toInt() shl 8) or
                fileBytes[offset + 3].toInt()
        offset += 4

        // Read the encrypted AES key
        val encryptedAESKey = fileBytes.copyOfRange(offset, offset + aesKeyLength)
        offset += aesKeyLength

        // Read the IV size (1 byte)
        val ivLength = fileBytes[offset].toInt()
        offset += 1

        // Read the IV
        val iv = fileBytes.copyOfRange(offset, offset + ivLength)
        offset += ivLength

        // Read the encrypted data size (4 bytes)
        val encryptedDataSize = (fileBytes[offset].toInt() shl 24) or
                (fileBytes[offset + 1].toInt() shl 16) or
                (fileBytes[offset + 2].toInt() shl 8) or
                fileBytes[offset + 3].toInt()
        offset += 4

        // Read the encrypted data
        val encryptedData = fileBytes.copyOfRange(offset, offset + encryptedDataSize)

        // Decrypt the AES key with the RSA private key
        val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)

        // Use the decrypted AES key to decrypt the data
        val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(aesKeyBytes, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val decryptedBytes = aesCipher.doFinal(encryptedData)

        return String(decryptedBytes)
    }


    private fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    private fun getKeyPair(): KeyPair {
        return if (keyStore.containsAlias(AndroidKMMCrypto.alias)) {
            val privateKey = keyStore.getKey(AndroidKMMCrypto.alias, null)
            val publicKey = keyStore.getCertificate(AndroidKMMCrypto.alias).publicKey
            KeyPair(publicKey, privateKey as java.security.PrivateKey)
        } else {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
            )
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    AndroidKMMCrypto.alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build()
            )
            val keyPair = keyPairGenerator.generateKeyPair()
            KeyPair(keyPair.public, keyPair.private)
        }
    }
}
