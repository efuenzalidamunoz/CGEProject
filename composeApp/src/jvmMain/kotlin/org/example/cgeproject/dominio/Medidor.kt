package org.example.cgeproject.dominio

import java.util.Date

open class Medidor(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    private val codigo: String,
    private val direccionSuministro: String,
    private val activo: Boolean,
    private val idCliente: String
): EntidadBase(id, createdAt, updatedAt) {

    /** Getters **/
    fun getCodigo(): String = codigo
    fun getDireccionSuministro(): String = direccionSuministro
    fun getActivo(): Boolean = activo
    fun getIdCliente(): String = idCliente


    /** Obtiene el tipo del medidior **/
    open fun tipo(): String {
        return "Medidor Genérico"
    }


}