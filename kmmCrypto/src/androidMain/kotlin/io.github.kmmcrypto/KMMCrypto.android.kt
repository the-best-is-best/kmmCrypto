package io.github.kmmcrypto

import android.content.Context
import android.os.Build



class AndroidKMMCrypto {

    companion object {
        @Volatile
        internal lateinit var applicationContext: Context

        internal lateinit var alias: String

        fun init(alias: String) {
            this.alias = alias
        }
    }
}

actual class KMMCrypto {

    actual fun saveData(key: String, group: String, data: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CryptoKeyStore().encryptAndSave(key, group, data)
        } else {
            CryptoData().encryptAndSave(key, group, data)
        }
    }

    actual suspend fun loadData(key: String, group: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CryptoKeyStore().retrieveAndDecrypt(key, group)
        } else {
            CryptoData().retrieveAndDecrypt(key, group)

        }
    }

    actual fun deleteData(key: String, group: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CryptoKeyStore().deleteData(
                key, group
            )

        } else {
            CryptoData().deleteData(key, group)
        }
    }
}