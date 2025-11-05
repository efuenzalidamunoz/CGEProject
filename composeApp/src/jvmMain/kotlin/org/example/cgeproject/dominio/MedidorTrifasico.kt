package org.example.cgeproject.dominio

import kotlinx.datetime.Instant // Tipo corregido

class MedidorTrifasico constructor(
    id: String,
    createdAt: Instant,
    updatedAt: Instant,
    codigo: String,
    direccionSuministro: String,
    activo: Boolean,
    idCliente: String,
    private val potenciaMaxKw: Double,
    private val factorPotencia: Double,
): Medidor(id, createdAt, updatedAt, codigo, direccionSuministro, activo, idCliente) {
    
    override fun tipo(): String {
        return "Trifásico"
    }

    fun getPotenciaMaxKw() : Double = potenciaMaxKw
    fun getFactorPotencia() : Double = factorPotencia
}