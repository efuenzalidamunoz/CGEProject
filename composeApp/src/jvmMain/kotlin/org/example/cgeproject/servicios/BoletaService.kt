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
    private val tarifas: TarifaService,
    private val pdf: PdfService
) {

    fun emitirBoletaMensual(rutCliente: String, mes: Int, anio: Int): Boleta {
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")
        val consumoKwh = calcularKwhClienteMes(rutCliente, mes, anio)
        val tarifa = tarifas.tarifaPara(cliente)
        val detalleTarifa = tarifa.calcular(consumoKwh)

        val boleta = Boleta(
            id = "B-" + System.currentTimeMillis(),
            createdAt = Date(),
            updatedAt = Date(),
            idCliente = cliente.getRut(),
            anio = anio,
            mes = mes,
            kwhTotal = consumoKwh,
            detalle = detalleTarifa,
            estado = EstadoBoleta.PENDIENTE
        )

        return boletas.guardar(boleta)
    }

    fun eliminarBoleta(rut: String, anio: Int, mes: Int) {
        boletas.eliminarBoleta(rut, anio, mes)
    }

    fun calcularKwhClienteMes(rutCliente: String, mes: Int, anio: Int): Double {
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")
        val medidoresCliente = medidores.listarPorCliente(cliente.getRut())
        var consumoTotal = 0.0

        for (medidor in medidoresCliente) {
            val lecturasMes = lecturas.listarPorMedidorMes(medidor.getCodigo(), anio, mes)
            if (lecturasMes.isNotEmpty()) {
                val primeraLectura = lecturasMes.first().getKwhLeidos()
                val ultimaLectura = lecturasMes.last().getKwhLeidos()
                consumoTotal += ultimaLectura - primeraLectura
            }
        }

        return consumoTotal
    }

    fun exportarPdfClienteMes(rutCliente: String, mes: Int, anio: Int): ByteArray {
        val boleta = boletas.obtener(rutCliente, anio, mes) ?: throw Exception("Boleta no encontrada")
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")
        return pdf.generarPdf(listOf(boleta), mapOf(rutCliente to cliente))
    }
}