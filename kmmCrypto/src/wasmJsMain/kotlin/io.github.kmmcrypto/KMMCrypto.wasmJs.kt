package io.github.kmmcrypto

import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class KMMCrypto {

    actual fun saveData(key: String, group: String, data: String) {
        // Create a unique key for storage
        val storageKey = "$group-$key"
        // Save data to localStorage
        window.localStorage.setItem(storageKey, data)
    }

    actual suspend fun loadData(key: String, group: String): String? {
        return withContext(Dispatchers.Main) {
            // Create a unique key for retrieval
            val storageKey = "$group-$key"
            // Retrieve data from localStorage
            window.localStorage.getItem(storageKey)
        }
    }
}
