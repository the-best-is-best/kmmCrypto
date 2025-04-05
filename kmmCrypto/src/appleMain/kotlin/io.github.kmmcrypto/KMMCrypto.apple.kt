package io.github.kmmcrypto

import io.native.kmmcrypto.IOSCryptoManager
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@OptIn(ExperimentalForeignApi::class)
actual class KMMCrypto {
    actual fun saveData(key: String, group: String, data: String) {

        IOSCryptoManager.saveWithService(
            key, group, data
        ) { e ->
            if (e != null) {
                return@saveWithService
            }
        }

    }

    fun saveDataType(key: String, group: String, data: NSData) {
        IOSCryptoManager.saveDataTypeWithService(
            key, group, data
        ) { e ->
            if (e != null) {
                return@saveDataTypeWithService
            }

        }
    }


    actual suspend fun loadData(key: String, group: String): String? {
        return suspendCancellableCoroutine { continuation ->
            IOSCryptoManager.getWithService(key, group) { v, e ->
                if (e == null) {
                    continuation.resume(v)  // Resume with the result
                } else {
                    if (e.code.toInt() == 1) {
                        continuation.resume(null)
                        return@getWithService
                    }

                    continuation.resumeWithException(Exception(e.localizedFailureReason))  // Resume with an exception
                }
            }
        }
    }

    suspend fun loadDataType(key: String, group: String): NSData? {
        return suspendCancellableCoroutine { continuation ->
            IOSCryptoManager.getDataTypeWithService(key, group) { v, e ->
                if (e == null) {
                    continuation.resume(v)  // Resume with the result
                } else {

                    continuation.resumeWithException(RuntimeException(e.localizedFailureReason))  // Resume with an exception
                }
            }
        }
    }

    actual fun deleteData(key: String, group: String) {
        IOSCryptoManager.deleteDataWithService(key, group)
    }

}

