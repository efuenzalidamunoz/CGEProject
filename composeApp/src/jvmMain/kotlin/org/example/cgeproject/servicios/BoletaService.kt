package org.example.cgeproject.servicios

import kotlinx.datetime.Clock
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.EstadoBoleta
import org.example.cgeproject.persistencia.BoletaRepositorio
import org.example.cgeproject.persistencia.ClienteRepositorio
import org.example.cgeproject.persistencia.LecturaRepositorio
import org.example.cgeproject.persistencia.MedidorRepositorio

class BoletaService(
    private val clientes: ClienteRepositorio,
    private val medidores: MedidorRepositorio,
    private val lecturas: LecturaRepositorio,
    private val boletas: BoletaRepositorio,
    private val tarifas: TarifaService
) {

    suspend fun generarBoleta(rutCliente: String, anio: Int, mes: Int): Boleta {
        val cliente = clientes.obtenerPorRut(rutCliente) ?: throw Exception("Cliente no encontrado")

        val consumoTotalKwh = calcularKwhClienteMes(rutCliente, anio, mes)

        val tarifa = tarifas.tarifaPara(cliente)
        val detalleTarifa = tarifa.calcular(consumoTotalKwh)

        val nuevaBoleta = Boleta(
            id = "bol-${rutCliente}-${anio}-${mes}",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            idCliente = rutCliente,
            anio = anio,
            mes = mes,
            kwhTotal = consumoTotalKwh,
            detalle = detalleTarifa,
            estado = EstadoBoleta.EMITIDA
        )

        return boletas.guardar(nuevaBoleta)
    }

    suspend fun calcularKwhClienteMes(rutCliente: String, anio: Int, mes: Int): Double {
        val medidoresCliente = medidores.listarPorCliente(rutCliente)
        if (medidoresCliente.isEmpty()) return 0.0

        var consumoTotal = 0.0

        for (medidor in medidoresCliente) {
            val lecturaActual = lecturas.listarPorMedidorMes(medidor.getCodigo(), anio, mes).maxByOrNull { it.getKwhLeidos() }?.getKwhLeidos() ?: 0.0

            val (anioAnterior, mesAnterior) = if (mes == 1) (anio - 1 to 12) else (anio to mes - 1)
            val lecturaAnterior = lecturas.listarPorMedidorMes(medidor.getCodigo(), anioAnterior, mesAnterior).maxByOrNull { it.getKwhLeidos() }?.getKwhLeidos() ?: 0.0

            val consumoMedidor = if (lecturaActual > lecturaAnterior) lecturaActual - lecturaAnterior else 0.0
            consumoTotal += consumoMedidor
        }

        return consumoTotal
    }

    suspend fun exportarPdfClienteMes(rutCliente: String, anio: Int, mes: Int, pdf: PdfService): ByteArray {
        val boleta = boletas.obtener(rutCliente, anio, mes) ?: throw Exception("Boleta no encontrada")
        return pdf.generarPdf(boleta)
    }
}