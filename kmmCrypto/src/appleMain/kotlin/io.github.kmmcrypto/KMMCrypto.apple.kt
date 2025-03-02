package io.github.kmmcrypto

import io.github.kmmcrypto.ios_crypto.IOSCrypto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@OptIn(ExperimentalForeignApi::class)
actual class KMMCrypto {
    actual fun saveData(key: String, group: String, data: String) {

        IOSCrypto.saveWithService(
            key, group, data
        ) { e ->
            if (e != null) {
                return@saveWithService
            }
        }

    }

    fun saveDataType(key: String, group: String, data: NSData) {
        IOSCrypto.saveDataTypeWithService(
            key, group, data
        ) { e ->
            if (e != null) {
                return@saveDataTypeWithService
            }

        }
    }


    actual suspend fun loadData(key: String, group: String): String? {
        return suspendCancellableCoroutine { continuation ->
            IOSCrypto.getWithService(key, group) { v, e ->
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
            IOSCrypto.getDataTypeWithService(key, group) { v, e ->
                if (e == null) {
                    continuation.resume(v)  // Resume with the result
                } else {

                    continuation.resumeWithException(RuntimeException(e.localizedFailureReason))  // Resume with an exception
                }
            }
        }
    }

    actual fun deleteData(key: String, group: String) {
        IOSCrypto.deleteDataWithService(key, group)
    }

}

