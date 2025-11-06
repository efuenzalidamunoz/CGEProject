package org.example.cgeproject.dominio


import kotlin.math.round

class TarifaResidencial(
    private val cargoFijo: Double,
    private val preciosPorTramo: Map<Int, Double>, // Clave: Límite superior del tramo en kWh
    private val ivaPorcentaje: Double = 0.19
) : Tarifa {

    override fun nombre(): String = "Tarifa Residencial"

    /**
     * Calcula el costo total de la energía consumida para una Tarifa Residencial.
     *
     * @param kwh La cantidad de kilovatios-hora (kWh) consumidos.
     * @return Un objeto [TarifaDetalle] que contiene el desglose del cálculo.
     */
    override fun calcular(kwh: Double): TarifaDetalle {
        val costoConsumo = calcularCostoConsumo(kwh)
        val subtotalConCargos = costoConsumo + cargoFijo
        val iva = round(subtotalConCargos * ivaPorcentaje)
        val total = subtotalConCargos + iva

        return TarifaDetalle(
            kwh = kwh,
            subtotal = costoConsumo,
            cargos = cargoFijo,
            iva = iva,
            total = total
        )
    }

    /**
     * Calcula el costo de la energía consumida aplicando los precios por tramo.
     *
     * @param kwh La cantidad de kilovatios-hora (kWh) consumidos.
     * @return El costo total de la energía consumida sin incluir cargos fijos ni IVA.
     */
    private fun calcularCostoConsumo(kwh: Double): Double {
        var costoTotal = 0.0
        var kwhRestante = kwh
        var limiteAnterior = 0
        // Tramos ordenados por límite de kWh
        val tramosOrdenados = preciosPorTramo.entries.sortedBy { it.key }

        for ((limite, precio) in tramosOrdenados) {
            if (kwhRestante <= 0) break

            val tamanoTramo = if (limite == Int.MAX_VALUE) {
                Double.POSITIVE_INFINITY
            } else {
                (limite - limiteAnterior).toDouble()
            }
            
            val kwhEnTramo = minOf(kwhRestante, tamanoTramo)

            costoTotal += kwhEnTramo * precio
            kwhRestante -= kwhEnTramo
            limiteAnterior = limite
        }

        return costoTotal
    }
}