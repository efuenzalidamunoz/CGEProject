package org.example.cgeproject.dominio

import java.util.Date

open class Medidor constructor(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    private val codigo: String,
    private val direccionSuministro: String,
    private val activo: Boolean,
    private val idCliente: String
): EntidadBase(id, createdAt, updatedAt) {
    open fun tipo(): String {
        return "Medidor Genérico"
    }

    fun getCodigo(): String = codigo
    fun getIdCliente(): String = idCliente
}