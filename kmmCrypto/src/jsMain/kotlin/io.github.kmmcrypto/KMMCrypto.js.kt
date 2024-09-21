package io.github.kmmcrypto

import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class KMMCrypto {


    actual fun saveData(key: String, group: String, data: String) {
        val encryptedData = encrypt(data)
        val storageKey = "$group-$key"
        window.localStorage.setItem(storageKey, encryptedData)
    }

    actual suspend fun loadData(key: String, group: String): String? {
        return withContext(Dispatchers.Main) {
            val storageKey = "$group-$key"
            val encryptedData = window.localStorage.getItem(storageKey)
            encryptedData?.let { decrypt(it) }
        }
    }

    private fun encrypt(data: String): String {
        // Encrypt using crypto-js
        return window.asDynamic().CryptoJS.AES.encrypt(data).toString()
    }

    private fun decrypt(encryptedData: String): String {
        // Decrypt using crypto-js
        val bytes = window.asDynamic().CryptoJS.AES.decrypt(encryptedData)
        return bytes.toString(window.asDynamic().CryptoJS.enc.Utf8) as String
    }
}
