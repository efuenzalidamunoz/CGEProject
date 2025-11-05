package org.example.cgeproject.dominio

class TarifaComercial(
    private val cargoFijo: Double,
    private val precioKwh: Double,
    private val recargoComercial: Double,
    private val iva: Double,
): Tarifa {
    fun getCargoFijo() : Double = cargoFijo
    fun getPrecioKwh() : Double = precioKwh
    fun getRecargoComercial() : Double = recargoComercial
    fun getIva() : Double = iva

    override fun nombre(): String {
        TODO("Not yet implemented")
    }

    override fun calcular(kwh: Double): TarifaDetalle {
        TODO("Not yet implemented")
    }
}