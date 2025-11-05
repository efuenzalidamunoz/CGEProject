package org.example.cgeproject.dominio

import kotlin.math.abs

class TarifaDetalle(
    var kwh: Double,
    var subtotal: Double,
    var cargos: Double,
    var iva: Double,
    var total: Double
) {
    init {
        require(kwh >= 0.0) { "Kwh no puede ser negativo" }
        require(subtotal >= 0.0) { "subtotal no puede ser negativo" }
        require(cargos >= 0.0) { "cargos no puede ser negativo" }
        require(iva >= 0.0) { "iva no puede ser negativo" }
        val suma = subtotal + cargos + iva
        require(abs(total - suma) <= 0.0001) { "total debe ser igual a subtotal + cargos + iva "}
    }
}