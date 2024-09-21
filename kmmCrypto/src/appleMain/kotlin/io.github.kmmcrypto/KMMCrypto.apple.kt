package io.github.kmmcrypto

import cocoapods.KServices.KServices
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@OptIn(ExperimentalForeignApi::class)
actual class KMMCrypto {
    actual fun saveData(key: String, group: String, data: String) {

        KServices.saveWithService(
            key, group, data
        ) {
            println("data saved")
        }

    }

    fun saveDataType(key: String, group: String, data: NSData) {
        KServices.saveDataTypeWithService(
            key, group, data
        ) {
            println("data saved")

        }
    }


    actual suspend fun loadData(key: String, group: String): String? {
        return suspendCancellableCoroutine { continuation ->
            KServices.getWithService("test", "man") { v, e ->
                if (e == null) {
                    continuation.resume(v)  // Resume with the result
                } else {
                    val throwable = e as? Throwable ?: RuntimeException("Unknown error occurred")

                    continuation.resumeWithException(throwable)  // Resume with an exception
                }
            }
        }
    }

    suspend fun loadDataType(key: String, group: String): NSData? {
        return suspendCancellableCoroutine { continuation ->
            KServices.getDataTypeWithService(key, group) { v, e ->
                if (e == null) {
                    continuation.resume(v)  // Resume with the result
                } else {
                    val throwable = e as? Throwable ?: RuntimeException("Unknown error occurred")

                    continuation.resumeWithException(throwable)  // Resume with an exception
                }
            }
        }
    }

}