package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

class LecturaRepoImpl: LecturaRepositorio {
    // Usa la base de datos central en memoria
    override suspend fun registrar(l: LecturaConsumo): LecturaConsumo {
        TODO("Not yet implemented")
    }

    override suspend fun listarPorMedidorMes(
        idMedidor: String,
        anio: Int,
        mes: Int
    ): List<LecturaConsumo> {
        TODO("Not yet implemented")
    }

    override suspend fun ultimaLectura(idMedidor: String): LecturaConsumo? {
        TODO("Not yet implemented")
    }

}