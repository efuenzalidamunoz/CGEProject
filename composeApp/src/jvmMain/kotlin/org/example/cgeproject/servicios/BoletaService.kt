package org.example.cgeproject.servicios

import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.EstadoBoleta
import org.example.cgeproject.persistencia.BoletaRepositorio
import org.example.cgeproject.persistencia.ClienteRepositorio
import org.example.cgeproject.persistencia.LecturaRepositorio
import org.example.cgeproject.persistencia.MedidorRepositorio
import java.util.Date

class BoletaService(
    private val clientes: ClienteRepositorio,
    private val medidores: MedidorRepositorio,
    private val lecturas: LecturaRepositorio,
    private val boletas: BoletaRepositorio,
    private val tarifas: TarifaService
) {

    fun emitirBoletaMensual(rutCliente: String, mes: Int, anio: Int): Boleta {
        TODO("Not yet implemented")
    }

    fun calcularKwhClienteMes(rutCliente: String, mes: Int, anio: Int): Double {
        TODO("Not yet implemented")
    }

    fun exportarPdfClienteMes(rutCliente: String, mes: Int, anio: Int): ByteArray {
        TODO("Not yet implemented")
    }
}