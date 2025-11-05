package org.example.cgeproject.dominio

interface Tarifa{
    fun nombre(): String
    fun calcular(kwh: Double): TarifaDetalle
}