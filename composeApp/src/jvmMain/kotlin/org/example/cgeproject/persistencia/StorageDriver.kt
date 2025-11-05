package org.example.cgeproject.persistencia

interface StorageDriver {
    fun put(key: String, data: ByteArray): Boolean
    fun get(key: String): ByteArray?
    fun keys(prefix: String): List<String>
    fun remove(key: String): Boolean
}