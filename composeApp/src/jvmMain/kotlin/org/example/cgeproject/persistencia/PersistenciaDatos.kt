package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.EstadoCliente
import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico
import java.util.Date
import org.example.cgeproject.dominio.TarifaDetalle
import org.example.cgeproject.dominio.EstadoBoleta
import org.example.cgeproject.dominio.TipoTarifa

/**
 * Persistencia basada en CSV (archivos por entidad).
 *
 * CSV simple — campos separados por coma. NO manejará comas dentro de los campos.
 *
 * Archivos:
 *  - clientes.csv
 *  - medidores.csv
 *  - lecturas.csv
 *  - boletas.csv
 *
 * Formatos (cabeceras comentadas abajo)
 */
class PersistenciaDatos(private val driver: StorageDriver) {

    companion object {
        private const val CLIENTES_KEY = "clientes"
        private const val MEDIDORES_KEY = "medidores"
        private const val LECTURAS_KEY = "lecturas"
        private const val BOLETAS_KEY = "boletas"

        private const val HEADER_CLIENTES = "rut,nombre,email,direccionFacturacion,estado,tipoTarifa"
        // medidor: id,createdAt,updatedAt,codigo,direccionSuministro,activo,idCliente,tipo,potenciaMaxKw,factorPotencia
        private const val HEADER_MEDIDORES = "id,createdAt,updatedAt,codigo,direccionSuministro,activo,idCliente,tipo,potenciaMaxKw,factorPotencia"
        // lectura: id,createdAt,updatedAt,idMedidor,anio,mes,kwhLeidos
        private const val HEADER_LECTURAS = "id,createdAt,updatedAt,idMedidor,anio,mes,kwhLeidos"
        // boleta: id,createdAt,updatedAt,idCliente,anio,mes,kwhTotal,detalle_subtotal,detalle_cargos,detalle_iva,detalle_total,estado
        private const val HEADER_BOLETAS = "id,createdAt,updatedAt,idCliente,anio,mes,kwhTotal,detalle_subtotal,detalle_cargos,detalle_iva,detalle_total,estado"
    }

    init {
        inicializar(CLIENTES_KEY, HEADER_CLIENTES)
        inicializar(MEDIDORES_KEY, HEADER_MEDIDORES)
        inicializar(LECTURAS_KEY, HEADER_LECTURAS)
        inicializar(BOLETAS_KEY, HEADER_BOLETAS)
    }

    /** Crea el archivo CSV si es que no existe **/
    private fun inicializar(key: String, header: String) {
        if (driver.get(key) == null) {
            driver.put(key, (header + "\n").toByteArray(Charsets.UTF_8))
        }
    }

    /** Lee el contenido del .csv **/
    private fun leerCSV(key: String): List<String> {
        val bytes = driver.get(key) ?: return emptyList()
        return String(bytes, Charsets.UTF_8)
            .lines()
            .filter { it.isNotBlank() }
    }

    /** Ocupamos para escribir dentro del .csv */
    private fun escribirCSV(key: String, lineas: List<String>): Boolean {
        val contenido = lineas.joinToString("\n") + "\n"
        return driver.put(key, contenido.toByteArray(Charsets.UTF_8))
    }

    private fun agregarLineaCSV(key: String, linea: String): Boolean {
        val lineas = leerCSV(key).toMutableList()
        lineas.add(linea)
        return escribirCSV(key, lineas)
    }

    /** Guarda el cliente dentro del .csv **/
    fun guardarCliente(cliente: Cliente): Boolean {
        // Guardamos rut,nombre,email,direccionFacturacion,estado,tipoTarifa
        val estadoStr = cliente.getEstado().toString()
        val tipoTarifaStr = cliente.getTipoTarifa().toString()
        val linea = listOf(
            cliente.getRut(), // asumo getRut() en Persona/Cliente; cambia si tu getter tiene otro nombre
            cliente.getNombre(),
            cliente.getEmail(),
            cliente.getDireccionFacturacion(),
            estadoStr,
            tipoTarifaStr
        ).joinToString(",")
        return agregarLineaCSV(CLIENTES_KEY, linea)
    }

    fun obtenerClientes(): List<Cliente> {
        val lines = leerCSV(CLIENTES_KEY).drop(1)
        // Primero cargar todas las boletas y medidores para poder asociarlas
        val boletas = obtenerBoletas()
        val medidores = obtenerMedidores()
        return lines.map { linea ->
            val p = linea.split(",")
            val rut = p.getOrElse(0) { "" }
            val nombre = p.getOrElse(1) { "" }
            val email = p.getOrElse(2) { "" }
            val direccion = p.getOrElse(3) { "" }
            val estadoName = p.getOrElse(4) { "" }
            val tipoTarifaName = p.getOrElse(5) { "RESIDENCIAL" } // Valor por defecto

            // asociar boletas y medidores por idCliente (rut)
            val boletasDelCliente = boletas.filter { it.getIdCliente() == rut }.toMutableList()
            val medidoresDelCliente = medidores.filter { it.getIdCliente() == rut }.toMutableList()

            // interpretar enum EstadoCliente
            val estado = try {
                EstadoCliente.valueOf(estadoName)
            } catch (ex: Exception) {
                EstadoCliente.ACTIVO // valor por defecto; ajusta según tu enum
            }

            // interpretar enum TipoTarifa
            val tipoTarifa = try {
                TipoTarifa.valueOf(tipoTarifaName)
            } catch (ex: Exception) {
                TipoTarifa.RESIDENCIAL // valor por defecto
            }

            // crear Cliente usando constructor
            Cliente(rut, nombre, email, direccion, estado, tipoTarifa, boletasDelCliente.toMutableList(), medidoresDelCliente.toMutableList())
        }
    }

    fun buscarClientePorRut(rut: String): Cliente? = obtenerClientes().find { it.getRut() == rut }

    fun eliminarCliente(rut: String): Boolean {
        val lines = leerCSV(CLIENTES_KEY).drop(1).toMutableList()
        val inicialSize = lines.size
        lines.removeIf { it.split(",").firstOrNull() == rut }
        if (lines.size == inicialSize) return false
        val headerAnd = mutableListOf(HEADER_CLIENTES)
        headerAnd.addAll(lines)
        return escribirCSV(CLIENTES_KEY, headerAnd)
    }

    /** Guarda el medidor en el .csv **/
    fun guardarMedidor(m: Medidor): Boolean {
        // id,createdAt,updatedAt,codigo,direccionSuministro,activo,idCliente,tipo,potenciaMaxKw,factorPotencia
        val potencia = when (m) {
            is MedidorMonofasico -> m.getPotenciaMaxKw().toString()
            is MedidorTrifasico -> m.getPotenciaMaxKw().toString()
            else -> ""
        }
        val factor = when (m) {
            is MedidorTrifasico -> m.getFactorPotencia().toString()
            else -> ""
        }
        val linea = listOf(
            m.getId(), // asumo getId()
            m.getCreatedAt().time.toString(),
            m.getUpdatedAt().time.toString(),
            m.getCodigo(),
            m.getDireccionSuministro(),
            m.getActivo().toString(),
            m.getIdCliente(),
            m.tipo(),
            potencia,
            factor
        ).joinToString(",")
        return agregarLineaCSV(MEDIDORES_KEY, linea)
    }

    fun obtenerMedidores(): List<Medidor> {
        return leerCSV(MEDIDORES_KEY)
            .drop(1)
            .map { linea ->
                val p = linea.split(",")
                val id = p.getOrElse(0) { "" }
                val createdAt = Date(p.getOrElse(1) { "0" }.toLong())
                val updatedAt = Date(p.getOrElse(2) { "0" }.toLong())
                val codigo = p.getOrElse(3) { "" }
                val direccion = p.getOrElse(4) { "" }
                val activo = p.getOrElse(5) { "false" }.toBoolean()
                val idCliente = p.getOrElse(6) { "" }
                val tipo = p.getOrElse(7) { "" }
                val potencia = p.getOrElse(8) { "0.0" }.toDoubleOrNull() ?: 0.0
                val factor = p.getOrElse(9) { "1.0" }.toDoubleOrNull() ?: 1.0

                if (tipo.equals("Monofásico", ignoreCase = true) || tipo.equals("Monofasico", ignoreCase = true) || tipo.equals("Monofásico", ignoreCase = true)) {
                    MedidorMonofasico(id, createdAt, updatedAt, codigo, direccion, activo, idCliente, potencia)
                } else {
                    MedidorTrifasico(id, createdAt, updatedAt, codigo, direccion, activo, idCliente, potencia, factor)
                }
            }
    }

    fun buscarMedidorPorSerie(codigo: String): Medidor? = obtenerMedidores().find { it.getCodigo() == codigo }

    fun eliminarMedidor(id: String): Boolean {
        val lines = leerCSV(MEDIDORES_KEY).drop(1).toMutableList()
        val inicialSize = lines.size
        lines.removeIf { it.split(",").firstOrNull() == id }
        if (lines.size == inicialSize) return false
        val headerAnd = mutableListOf(HEADER_MEDIDORES)
        headerAnd.addAll(lines)
        return escribirCSV(MEDIDORES_KEY, headerAnd)
    }

    fun guardarLectura(l: LecturaConsumo): Boolean {
        // id,createdAt,updatedAt,idMedidor,anio,mes,kwhLeidos
        val linea = listOf(
            l.getId(),
            l.getCreatedAt().time.toString(),
            l.getUpdatedAt().time.toString(),
            l.getIdMedidor(),
            l.getAnioLectura().toString(),
            l.getMesLectura().toString(),
            l.getKwhLeidos().toString()
        ).joinToString(",")
        return agregarLineaCSV(LECTURAS_KEY, linea)
    }

    fun guardarTodasLasLecturas(lecturas: List<LecturaConsumo>): Boolean {
        var allSaved = true
        for (lectura in lecturas) {
            if (!guardarLectura(lectura)) {
                allSaved = false
            }
        }
        return allSaved
    }

    fun obtenerLecturas(): List<LecturaConsumo> {
        return leerCSV(LECTURAS_KEY)
            .drop(1)
            .map { linea ->
                val p = linea.split(",")
                LecturaConsumo(
                    p.getOrElse(0) { "" },
                    Date(p.getOrElse(1) { "0" }.toLong()),
                    Date(p.getOrElse(2) { "0" }.toLong()),
                    p.getOrElse(3) { "" },
                    p.getOrElse(4) { "0" }.toInt(),
                    p.getOrElse(5) { "0" }.toInt(),
                    p.getOrElse(6) { "0.0" }.toDouble()
                )
            }
    }

    fun eliminarLectura(id: String): Boolean {
        val lines = leerCSV(LECTURAS_KEY).drop(1).toMutableList()
        val inicialSize = lines.size
        lines.removeIf { it.split(",").firstOrNull() == id }
        if (lines.size == inicialSize) return false
        val headerAnd = mutableListOf(HEADER_LECTURAS)
        headerAnd.addAll(lines)
        return escribirCSV(LECTURAS_KEY, headerAnd)
    }

    /** Guardamos las boletas dentro del csv **/
    fun guardarBoleta(b: Boleta): Boolean {
        // id,createdAt,updatedAt,idCliente,anio,mes,kwhTotal,detalle_subtotal,detalle_cargos,detalle_iva,detalle_total,estado
        val d = b.getDetalle()
        val linea = listOf(
            b.getId(),
            b.getCreatedAt().time.toString(),
            b.getUpdatedAt().time.toString(),
            b.getIdCliente(),
            b.getAnio().toString(),
            b.getMes().toString(),
            b.getKwhTotal().toString(),
            d.subtotal.toString(),
            d.cargos.toString(),
            d.iva.toString(),
            d.total.toString(),
            b.getEstado().toString()
        ).joinToString(",")
        return agregarLineaCSV(BOLETAS_KEY, linea)
    }

    fun obtenerBoletas(): List<Boleta> {
        return leerCSV(BOLETAS_KEY)
            .drop(1)
            .map { linea ->
                val p = linea.split(",")
                val id = p.getOrElse(0) { "" }
                val createdAt = Date(p.getOrElse(1) { "0" }.toLong())
                val updatedAt = Date(p.getOrElse(2) { "0" }.toLong())
                val idCliente = p.getOrElse(3) { "" }
                val anio = p.getOrElse(4) { "0" }.toInt()
                val mes = p.getOrElse(5) { "0" }.toInt()
                val kwh = p.getOrElse(6) { "0.0" }.toDouble()
                val subtotal = p.getOrElse(7) { "0.0" }.toDoubleOrNull() ?: 0.0
                val cargos = p.getOrElse(8) { "0.0" }.toDoubleOrNull() ?: 0.0
                val iva = p.getOrElse(9) { "0.0" }.toDoubleOrNull() ?: 0.0
                val total = p.getOrElse(10) { "0.0" }.toDoubleOrNull() ?: 0.0
                val estadoStr = p.getOrElse(11) { "" }

                val detalle = TarifaDetalle(kwh, subtotal, cargos, iva, total)
                val estado = try { EstadoBoleta.valueOf(estadoStr) } catch (ex: Exception) { EstadoBoleta.PENDIENTE }

                Boleta(id, createdAt, updatedAt, idCliente, anio, mes, kwh, detalle, estado)
            }
    }

    fun eliminarBoleta(id: String): Boolean {
        val lines = leerCSV(BOLETAS_KEY).drop(1).toMutableList()
        val inicialSize = lines.size
        lines.removeIf {
            val p = it.split(",")
            p.getOrElse(0) { "" } == id
        }
        if (lines.size == inicialSize) return false
        val headerAnd = mutableListOf(HEADER_BOLETAS)
        headerAnd.addAll(lines)
        return escribirCSV(BOLETAS_KEY, headerAnd)
    }

    // Utilidad: listar keys (delegado al driver)
    fun keys(prefix: String): List<String> = driver.keys(prefix)
}