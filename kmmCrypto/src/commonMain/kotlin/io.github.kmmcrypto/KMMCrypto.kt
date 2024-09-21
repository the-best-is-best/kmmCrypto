package io.github.kmmcrypto

expect class KMMCrypto() {
    fun saveData(key: String, group: String, data: String)
    suspend fun loadData(key: String, group: String): String?

}