package io.github.kmmcrypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.M)
internal class CryptoKeyStore {

    companion object {
        private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
        private const val AES_ALGORITHM = "AES"
        private const val AES_KEY_SIZE = 256
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12 // Recommended size for GCM
    }

    private val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply { load(null) }

    // Encrypt and save data using AES encryption
    fun encryptAndSave(key: String, group: String, data: String): ByteArray {
        try {
            val keyPair = getKeyPair()

            // Generate AES key
            val aesKey = generateAESKey()

            // Encrypt the data with AES
            val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val encryptedData = aesCipher.doFinal(data.toByteArray())

            // Encrypt the AES key with the RSA public key
            val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
            rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
            val encryptedAESKey = rsaCipher.doFinal(aesKey.encoded)

            // Log sizes for debugging

            // Get the directory path for the group folder
            val groupDir = File(AndroidKMMCrypto.activity.filesDir, group).apply { mkdirs() }

            // Create the file in the specified group folder
            val file = File(groupDir, key)

            // Write the encrypted AES key, IV, and encrypted data to the file
            file.outputStream().use { fos ->
                fos.write(encryptedAESKey.size shr 24 and 0xFF)
                fos.write(encryptedAESKey.size shr 16 and 0xFF)
                fos.write(encryptedAESKey.size shr 8 and 0xFF)
                fos.write(encryptedAESKey.size and 0xFF)
                fos.write(encryptedAESKey)
                fos.write(iv.size)
                fos.write(iv)
                fos.write(encryptedData.size shr 24 and 0xFF)
                fos.write(encryptedData.size shr 16 and 0xFF)
                fos.write(encryptedData.size shr 8 and 0xFF)
                fos.write(encryptedData.size and 0xFF)
                fos.write(encryptedData)
            }

            return encryptedData
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }

    // Retrieve and decrypt data using AES decryption
    fun retrieveAndDecrypt(key: String, group: String): String? {
        try {
            val keyPair = getKeyPair()

            // Construct the file path using the group directory and key as the filename
            val file = File(AndroidKMMCrypto.activity.filesDir, "$group/$key")

            // Check if the file exists
            if (!file.exists()) return null

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
            val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
            rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
            val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)

            // Use the decrypted AES key to decrypt the data
            val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
            val secretKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = aesCipher.doFinal(encryptedData)

            return String(decryptedBytes)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }

    private fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(AES_KEY_SIZE, SecureRandom())
        return keyGenerator.generateKey()
    }

    private fun getKeyPair(): KeyPair {
        return if (keyStore.containsAlias(AndroidKMMCrypto.alias)) {
            val privateKey = keyStore.getKey(AndroidKMMCrypto.alias, null)
            val publicKey = keyStore.getCertificate(AndroidKMMCrypto.alias).publicKey
            KeyPair(publicKey, privateKey as java.security.PrivateKey)
        } else {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_PROVIDER
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
            keyPairGenerator.generateKeyPair()
        }
    }

    fun deleteData(key: String, group: String) {
        val file = File(AndroidKMMCrypto.activity.filesDir, "$group/$key")
        if (file.exists()) {
            file.delete()
        }
    }
}