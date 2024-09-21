package io.github.kmmcrypto

import java.io.File

internal class FileExist {
    fun isFileExist(key: String, group: String): Boolean {
        val directory = File(AndroidKMMCrypto.activity.filesDir, group)
        val file = File(directory, key)

        // Check if the file exists
        return file.exists()
    }
}