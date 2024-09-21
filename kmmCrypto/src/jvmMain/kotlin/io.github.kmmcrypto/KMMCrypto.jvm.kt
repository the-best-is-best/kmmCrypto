package io.github.kmmcrypto


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.Key
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

private val key: Key = generateSecretKey()

private fun generateSecretKey(): Key {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256) // 256-bit AES key
    return keyGen.generateKey()
}

private fun encrypt(data: String): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) } // Generate a random IV
    cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
    val encryptedBytes = cipher.doFinal(data.toByteArray())
    val ivAndEncryptedData = iv + encryptedBytes // Prepend IV to encrypted data
    return Base64.getEncoder().encodeToString(ivAndEncryptedData)
}

private fun decrypt(encryptedData: String): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val ivAndEncryptedData = Base64.getDecoder().decode(encryptedData)
    val iv = ivAndEncryptedData.copyOfRange(0, 16) // Extract IV
    val encryptedBytes =
        ivAndEncryptedData.copyOfRange(16, ivAndEncryptedData.size) // Extract encrypted data
    cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
    val decryptedBytes = cipher.doFinal(encryptedBytes)
    return String(decryptedBytes)
}

actual class KMMCrypto {
    actual fun saveData(key: String, group: String, data: String) {
        val encryptedData = encrypt(data)
        try {
            val groupDir = Paths.get(group)
            if (!Files.exists(groupDir)) {
                Files.createDirectories(groupDir)
            }
            val filePath = groupDir.resolve("$key.txt")
            Files.write(
                filePath,
                encryptedData.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            // Set proper permissions here if needed
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    actual suspend fun loadData(key: String, group: String): String? {
        return withContext(Dispatchers.IO) {
            val filePath = Paths.get(group).resolve("$key.txt")
            if (Files.exists(filePath)) {
                val encryptedData = Files.readString(filePath)
                return@withContext decrypt(encryptedData)
            }
            null
        }
    }
}
