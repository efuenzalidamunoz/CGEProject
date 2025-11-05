package org.example.cgeproject.persistencia

import java.io.File
import java.nio.file.Paths

/**
 * Implementación JVM del StorageDriver que guarda en la carpeta "data" en la raíz del proyecto.
 * Ruta: <project-root>/data
 */
class FileSystemStorageDriver : StorageDriver {
    private val projectRoot = System.getProperty("user.dir") ?: "."
    private val dataDir = Paths.get(projectRoot, "data").toFile()

    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }

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

    override fun keys(prefix: String): List<String> {
        return dataDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith(prefix) && it.name.endsWith(".csv") }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

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

    // utilidad jvm-only
    fun getDataPath(): String = dataDir.absolutePath
}