package org.example.cgeproject.dominio

import java.util.Date

class LecturaConsumo(
    id: String,
    createdAt: Date,
    updatedAt: Date,
    private val idMedidor: String,
    private val anio: Int,
    private val mes: Int,
    private val kwhLeidos: Double
): EntidadBase(id, createdAt, updatedAt) {

    /** Getters **/
    fun getIdMedidor() : String = idMedidor
    fun getAnioLectura() : Int = anio
    fun getMesLectura() : Int = mes
    fun getKwhLeidos() : Double = kwhLeidos

}