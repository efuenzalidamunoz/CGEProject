package org.example.cgeproject.dominio

import kotlin.math.round


class TarifaResidencial : Tarifa {
    private val costoPorKwh = 150.0
    private val cargoFijo = 1000.0
    private val ivaPorcentaje = 0.19

    override fun nombre(): String = "Tarifa Residencial"

    override fun calcular(kwh: Double): TarifaDetalle {
        val costoConsumo = kwh * costoPorKwh
        val subtotal = costoConsumo + cargoFijo
        val iva = round(subtotal * ivaPorcentaje)
        val total = subtotal + iva

        return TarifaDetalle(
            kwh = kwh,
            subtotal = subtotal,
            cargos = cargoFijo,
            iva = iva,
            total = total
        )
    }
}