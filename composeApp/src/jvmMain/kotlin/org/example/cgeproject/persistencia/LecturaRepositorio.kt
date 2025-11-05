package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

interface LecturaRepositorio {
    suspend fun registrar(l: LecturaConsumo): LecturaConsumo
    suspend fun listarPorMedidorMes(idMedidor: String, anio: Int, mes: Int): List<LecturaConsumo>
    suspend fun ultimaLectura(idMedidor: String): LecturaConsumo?
}