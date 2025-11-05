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
        val subtotal = costoConsumo + cargoFijo
        val montoRecargo = subtotal * recargoComercial
        val subtotalConRecargo = subtotal + montoRecargo
        val montoIva = round(subtotalConRecargo * iva)
        val total = subtotalConRecargo + montoIva

        return TarifaDetalle(
            kwh = kwh,
            subtotal = subtotalConRecargo,
            cargos = cargoFijo + montoRecargo,
            iva = montoIva,
            total = total
        )
    }
}