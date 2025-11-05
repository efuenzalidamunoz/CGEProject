package org.example.cgeproject.persistencia

class PersistenciaDatos(
    private val driver: StorageDriver
) {
    companion object {
        private const val CLIENTES_KEY = "clientes"
        private const val MEDIDORES_KEY = "medidores"
        private const val LECTURAS_KEY = "lecturas"
        private const val BOLETAS_KEY = "boletas"

        private const val HEADER_CLIENTES = "rut,nombre,direccion,email,telefono"
        private const val HEADER_MEDIDORES = "numeroSerie,clienteRut,tipo,fechaInstalacion,datosAdicionales"
        private const val HEADER_LECTURAS =
            "id,medidorNumeroSerie,mes,anio,consumoKwh,lecturaAnterior,lecturaActual,fechaLectura"
        private const val HEADER_BOLETAS =
            "id,clienteRut,clienteNombre,mes,anio,consumoKwh,tipoTarifa,subtotal,cargos,iva,total,fechaEmision"
    }

    init {
        inicializarArchivo(CLIENTES_KEY, HEADER_CLIENTES)
        inicializarArchivo(MEDIDORES_KEY, HEADER_MEDIDORES)
        inicializarArchivo(LECTURAS_KEY, HEADER_LECTURAS)
        inicializarArchivo(BOLETAS_KEY, HEADER_BOLETAS)
    }

    private fun inicializarArchivo(key: String, header: String) {
        if (driver.get(key) == null) {
            driver.put(key, "$header\n".toByteArray())
        }
    }

    // Wrappers de driver (nombres acordes a la interfaz pedida)
    fun put(key: String, bytes: ByteArray): Boolean = driver.put(key, bytes)
    fun get(key: String): ByteArray? = driver.get(key)
    fun keys(prefix: String): List<String> = driver.keys(prefix)
    fun remove(key: String): Boolean = driver.remove(key)

    // Utilidad para leer CSV como texto
    private fun leerCSV(key: String): List<String> {
        val bytes = driver.get(key) ?: return emptyList()
        return String(bytes).lines().filter { it.isNotBlank() }
    }

    // Utilidad para escribir CSV (sobrescribe)
    private fun escribirCSV(key: String, lineas: List<String>): Boolean {
        val contenido = lineas.joinToString("\n") + "\n"
        return driver.put(key, contenido.toByteArray())
    }

    // Agregar línea al CSV (lee + agrega + escribe)
    private fun agregarLineaCSV(key: String, linea: String): Boolean {
        val lineasExistentes = leerCSV(key).toMutableList()
        lineasExistentes.add(linea)
        return escribirCSV(key, lineasExistentes)
    }
}