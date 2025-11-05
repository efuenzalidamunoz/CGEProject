package org.example.cgeproject.dominio

import org.example.cgeproject.servicios.PdfTable
import java.util.Date

class Boleta(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    private val idCliente: String,
    private val anio: Int,
    private val mes: Int,
    private val kwhTotal: Double,
    private val detalle: TarifaDetalle,
    private val estado: EstadoBoleta
): EntidadBase(id, createdAt, updatedAt), ExportablePDF {

    fun getIdCliente() : String = idCliente
    fun getAnio(): Int = anio
    fun getMes(): Int = mes
    fun getKwhTotal() : Double = kwhTotal
    fun getDetalle() : TarifaDetalle = detalle
    fun getEstado() : EstadoBoleta = estado

    override fun toPdfTable(): PdfTable {
        TODO("Not yet implemented")
    }


}