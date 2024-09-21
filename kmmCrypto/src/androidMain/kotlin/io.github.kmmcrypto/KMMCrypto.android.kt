package io.github.kmmcrypto

import android.os.Build
import androidx.activity.ComponentActivity


class AndroidKMMCrypto {
    companion object {
        internal lateinit var activity: ComponentActivity
        internal lateinit var alias: String

        fun init(context: ComponentActivity, alias: String) {
            this.activity = context
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


}