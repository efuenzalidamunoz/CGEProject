package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

class LecturaRepoImpl: LecturaRepositorio {
    override fun registrar(l: LecturaConsumo): LecturaConsumo {
        TODO("Not yet implemented")
    }

    override fun listarPorMedidorMes(
        idMedidor: String,
        anio: Int,
        mes: Int
    ): List<LecturaConsumo> {
        TODO("Not yet implemented")
    }

    override fun ultimaLectura(idMedidor: String): LecturaConsumo? {
        TODO("Not yet implemented")
    }

}