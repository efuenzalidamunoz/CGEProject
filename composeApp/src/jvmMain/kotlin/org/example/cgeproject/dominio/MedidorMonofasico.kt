package org.example.cgeproject.dominio

import java.util.Date

class MedidorMonofasico(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    codigo: String,
    direccionSuministro: String,
    activo: Boolean,
    idCliente: String,
    private val potenciaMaxKw: Double
): Medidor(id, createdAt, updatedAt, codigo, direccionSuministro, activo, idCliente) {

    /** Getters **/
    fun getPotenciaMaxKw() : Double = potenciaMaxKw

    /** Tipo del medidor **/
    override fun tipo(): String {
        return "Monofásico"
    }


}