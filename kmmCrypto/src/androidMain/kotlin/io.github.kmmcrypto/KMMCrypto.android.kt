package io.github.kmmcrypto

import android.content.Context


class AndroidKMMCrypto {

    companion object {
        @Volatile
        internal lateinit var applicationContext: Context

        @Volatile
        internal lateinit var alias: String

        fun init(alias: String) {
            this.alias = alias
        }
    }
}


actual class KMMCrypto {

    actual fun saveData(key: String, group: String, data: String) {
        CryptoKeyStore().encryptAndSave(key, group, data)

    }

    actual suspend fun loadData(key: String, group: String): String? {
        return CryptoKeyStore().retrieveAndDecrypt(key, group)

    }

    actual fun deleteData(key: String, group: String) {
        CryptoKeyStore().deleteData(
            key, group
        )
    }
}