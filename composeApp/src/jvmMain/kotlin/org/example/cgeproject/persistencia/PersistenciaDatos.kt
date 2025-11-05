package org.example.cgeproject.persistencia

class PersistenciaDatos(
    private val driver: StorageDriver
) {
    fun save(key: String, bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    fun read(key: String): ByteArray? {
        TODO("Not yet implemented")
    }

    fun list(prefix: String): List<String> {
        TODO("Not yet implemented")
        }

    fun delete(key: String): Boolean {
        TODO("Not yet implemented")
    }

}