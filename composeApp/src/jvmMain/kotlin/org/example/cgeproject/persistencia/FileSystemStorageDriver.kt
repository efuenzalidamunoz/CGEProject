package org.example.cgeproject.persistencia

import java.io.File
import java.nio.file.Paths

class FileSystemStorageDriver : StorageDriver {
    private val projectRoot = System.getProperty("user.dir") ?: "."
    private val dataDir = Paths.get(projectRoot, "data").toFile()

    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

    /** Almacena los datos asociados a una clave específica en el archivo CSV. **/
    override fun put(key: String, data: ByteArray): Boolean {
        return try {
            val file = File(dataDir, "$key.csv")
            file.writeBytes(data)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /** Recupera los datos asociados a una clave específica. **/
    override fun get(key: String): ByteArray? {
        return try {
            val file = File(dataDir, "$key.csv")
            if (!file.exists()) return null
            file.readBytes()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    /** Devuelve una lista de las llaves que inician con un pefijo y terminan en .csv **/
    override fun keys(prefix: String): List<String> {
        return dataDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(".csv") }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }


    /** Elimina un archivo del almacenamiento **/
    override fun remove(key: String): Boolean {
        return try {
            val file = File(dataDir, "$key.csv")
            if (!file.exists()) return false
            file.delete()
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun getDataPath(): String = dataDir.absolutePath
}