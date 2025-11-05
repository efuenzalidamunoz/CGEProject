package org.example.cgeproject.dominio

import kotlin.math.round

class TarifaComercial(
    private val cargoFijo: Double = 900.0,
    private val precioKwh: Double = 175.0,
    private val recargoComercial: Double = 0.05,
    private val iva: Double = 0.19,
): Tarifa {
    fun getCargoFijo() : Double = cargoFijo
    fun getPrecioKwh() : Double = precioKwh
    fun getRecargoComercial() : Double = recargoComercial
    fun getIva() : Double = iva

    override fun nombre(): String {
        return "Tarifa Comercial"
    }

    override fun calcular(kwh: Double): TarifaDetalle {
        val costoConsumo = kwh * precioKwh
        val montoRecargo = (costoConsumo + cargoFijo) * recargoComercial
        val cargosAdicionales = cargoFijo + montoRecargo
        val subtotal = costoConsumo
        val ivaCalculado = round((subtotal + cargosAdicionales) * iva)
        val total = subtotal + cargosAdicionales + ivaCalculado

        return TarifaDetalle(
            kwh = kwh,
            subtotal = subtotal,
            cargos = cargosAdicionales,
            iva = ivaCalculado,
            total = total
        )
    }
}