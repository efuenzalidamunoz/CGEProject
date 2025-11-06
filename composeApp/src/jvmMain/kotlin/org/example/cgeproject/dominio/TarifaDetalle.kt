package org.example.cgeproject.dominio

import kotlin.math.abs

class TarifaDetalle(
    var kwh: Double,
    var subtotal: Double,
    var cargos: Double,
    var iva: Double,
    var total: Double
) {
    // doc: Clase que representa el detalle de una tarifa de consumo eléctrico.
    // doc: @property kwh Cantidad de kilovatios-hora consumidos.
    // doc: @property subtotal Subtotal del consumo antes de cargos e IVA.
    // doc: @property cargos Monto total de cargos adicionales.
    // doc: @property iva Monto total del Impuesto al Valor Agregado (IVA).
    // doc: @property total Monto total a pagar, que debe ser la suma de subtotal, cargos e IVA.
    init {
        require(kwh >= 0.0) { "Kwh no puede ser negativo" }
        require(subtotal >= 0.0) { "subtotal no puede ser negativo" }
        require(cargos >= 0.0) { "cargos no puede ser negativo" }
        require(iva >= 0.0) { "iva no puede ser negativo" }
        val suma = subtotal + cargos + iva
        require(abs(total - suma) <= 0.0001) { "total debe ser igual a subtotal + cargos + iva " }
    }
}