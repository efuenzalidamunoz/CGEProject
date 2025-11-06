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

    fun emitirBoletaMensual(rutCliente: String, codigoMedidor: String, mes: Int, anio: Int, kwhConsumido: Double): Boleta {
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")

        // Verificación: No se emite una boleta si no hay consumo o lecturas para el período.
        if (kwhConsumido <= 0.0) {
            throw Exception("El consumo de kWh debe ser mayor a cero para emitir una boleta.")
        }

        val tarifa = tarifas.tarifaPara(cliente)
        val detalleTarifa = tarifa.calcular(kwhConsumido)

        val boleta = Boleta(
            id = "B-" + System.currentTimeMillis(),
            createdAt = Date(),
            updatedAt = Date(),
            idCliente = cliente.getRut(),
            anio = anio,
            mes = mes,
            kwhTotal = kwhConsumido,
            detalle = detalleTarifa,
            estado = EstadoBoleta.EMITIDA
        )

        return boletas.guardar(boleta)
    }

    fun eliminarBoleta(id: String) {
        boletas.eliminarBoleta(id)
    }


    /** Se exporta el PDF con los clientes en el mes especifico de la boleta */
    fun exportarPdfClienteMes(rutCliente: String, mes: Int, anio: Int): ByteArray {
        val boleta = boletas.obtener(rutCliente, anio, mes) ?: throw Exception("Boleta no encontrada")
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")
        return pdf.generarPdf(listOf(boleta), mapOf(rutCliente to cliente))
    }
}