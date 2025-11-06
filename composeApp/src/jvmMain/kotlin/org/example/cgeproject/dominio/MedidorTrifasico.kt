package org.example.cgeproject.dominio

import java.util.Date

class MedidorTrifasico(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    codigo: String,
    direccionSuministro: String,
    activo: Boolean,
    idCliente: String,
    private val potenciaMaxKw: Double,
    private val factorPotencia: Double,
): Medidor(id, createdAt, updatedAt, codigo, direccionSuministro, activo, idCliente) {

    /** Getters **/
    fun getPotenciaMaxKw() : Double = potenciaMaxKw
    fun getFactorPotencia() : Double = factorPotencia

    /** Tipo del medidor **/
    override fun tipo(): String {
        return "Trifásico"
    }

}