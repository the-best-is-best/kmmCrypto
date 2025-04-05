package io.github.kmmcrypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
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
        private val RSA_KEY_ALIAS = AndroidKMMCrypto.alias

    }

    private val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply { load(null) }

    fun encryptAndSave(key: String, group: String, data: String) {
        try {
            val keyPair = getKeyPair()

            val aesKey = generateAESKey()

            val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val encryptedData = aesCipher.doFinal(data.toByteArray())

            val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
            rsaCipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
            val encryptedAESKey = rsaCipher.doFinal(aesKey.encoded)

            val groupDir =
                File(AndroidKMMCrypto.applicationContext.filesDir, group).apply { mkdirs() }
            val file = File(groupDir, key)
            DataOutputStream(FileOutputStream(file)).use { dos ->
                dos.writeInt(encryptedAESKey.size)
                dos.write(encryptedAESKey)
                dos.writeInt(iv.size)
                dos.write(iv)
                dos.writeInt(encryptedData.size)
                dos.write(encryptedData)
            }
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }

    fun retrieveAndDecrypt(key: String, group: String): String? {
        try {
            val keyPair = getKeyPair()
            val file = File(AndroidKMMCrypto.applicationContext.filesDir, "$group/$key")

            if (!file.exists()) return null

            DataInputStream(FileInputStream(file)).use { dis ->
                val aesKeyLength = dis.readInt()
                val encryptedAESKey = ByteArray(aesKeyLength).also { dis.readFully(it) }

                val ivLength = dis.readInt()
                val iv = ByteArray(ivLength).also { dis.readFully(it) }

                val encryptedDataSize = dis.readInt()
                val encryptedData = ByteArray(encryptedDataSize).also { dis.readFully(it) }

                val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
                rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
                val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)

                val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
                val secretKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)
                aesCipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
                val decryptedBytes = aesCipher.doFinal(encryptedData)

                return String(decryptedBytes)
            }
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }

    fun deleteData(key: String, group: String) {
        val file = File(AndroidKMMCrypto.applicationContext.filesDir, "$group/$key")
        if (file.exists()) {
            file.delete()
        }
    }

    private fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(AES_KEY_SIZE, SecureRandom())
        return keyGenerator.generateKey()
    }

    private fun getKeyPair(): KeyPair {
        return if (keyStore.containsAlias(RSA_KEY_ALIAS)) {
            val privateKey = keyStore.getKey(RSA_KEY_ALIAS, null) as PrivateKey
            val publicKey = keyStore.getCertificate(RSA_KEY_ALIAS).publicKey
            KeyPair(publicKey, privateKey)
        } else {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_PROVIDER
            )
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    RSA_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build()
            )
            keyPairGenerator.generateKeyPair()
        }
    }
}
