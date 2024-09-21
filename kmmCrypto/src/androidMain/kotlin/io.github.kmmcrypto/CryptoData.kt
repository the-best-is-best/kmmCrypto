package io.github.kmmcrypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class CryptoData {
    private val ivSize = 16
    private val keySize = 32

    // Function to derive a key from the given string
    private fun deriveKey(key: String): ByteArray {
        val sha256 = MessageDigest.getInstance("SHA-256")
        return sha256.digest(key.toByteArray()).copyOf(keySize) // Ensure key is the right size
    }

    fun encryptAndSave(key: String, group: String, data: String): ByteArray {
        // Generate a random IV
        val iv = ByteArray(ivSize).apply { SecureRandom().nextBytes(this) }

        // Derive the key from the provided key string
        val secretKey = deriveKey(key)

        // Initialize the AES cipher in CBC mode with PKCS5Padding
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey, "AES"), IvParameterSpec(iv))

        // Encrypt the data
        val encryptedBytes = cipher.doFinal(data.toByteArray())

        // Define the directory and file path
        val directory = File(AndroidKMMCrypto.activity.filesDir, group)
        if (!directory.exists()) {
            directory.mkdirs()  // Create the directory if it doesn't exist
        }
        val file = File(directory, key)

        // Write the IV and encrypted data to the file
        FileOutputStream(file).use { outputStream ->
            outputStream.write(iv)  // Write the IV first
            outputStream.write(encryptedBytes)  // Then write the encrypted data
        }

        return encryptedBytes
    }

    fun retrieveAndDecrypt(key: String, group: String): String? {
        // Check if the file exists in the specified group directory
        if (!FileExist().isFileExist(key, group)) {
            return null
        }

        // Define the directory and file path
        val directory = File(AndroidKMMCrypto.activity.filesDir, group)
        val file = File(directory, key)

        // Read the IV and encrypted bytes from the file
        val inputStream = FileInputStream(file)
        val iv = ByteArray(ivSize)
        if (inputStream.read(iv) != ivSize) {
            inputStream.close()
            return null // Failed to read the full IV
        }

        val encryptedBytes = ByteArray(inputStream.available())
        inputStream.read(encryptedBytes)
        inputStream.close()

        // Derive the key from the provided key string
        val secretKey = deriveKey(key)

        // Initialize the AES cipher for decryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(secretKey, "AES"),
            IvParameterSpec(iv)
        )

        return try {
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            // Handle decryption errors
            null
        }
    }
}
