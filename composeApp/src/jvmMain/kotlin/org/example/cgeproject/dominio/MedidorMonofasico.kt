package org.example.cgeproject.dominio

import kotlinx.datetime.Instant // Tipo corregido

class MedidorMonofasico constructor(
    id: String,
    createdAt: Instant,
    updatedAt: Instant,
    codigo: String,
    direccionSuministro: String,
    activo: Boolean,
    idCliente: String,
    private val potenciaMaxKw: Double
): Medidor(id, createdAt, updatedAt, codigo, direccionSuministro, activo, idCliente) {

    override fun tipo(): String {
        return "Monofásico"
    }

    fun getPotenciaMaxKw() : Double = potenciaMaxKw
}