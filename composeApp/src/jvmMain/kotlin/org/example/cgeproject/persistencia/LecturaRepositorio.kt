package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

interface LecturaRepositorio {
    fun registrar(l: LecturaConsumo): LecturaConsumo
    fun listarPorMedidorMes(idMedidor: String, anio: Int, mes: Int): List<LecturaConsumo>
    fun ultimaLectura(idMedidor: String): LecturaConsumo?
    fun eliminarLectura(id: String): Boolean

}