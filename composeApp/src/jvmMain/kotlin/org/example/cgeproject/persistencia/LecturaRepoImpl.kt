package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

class LecturaRepoImpl(private val persist: PersistenciaDatos) : LecturaRepositorio {

    override fun registrar(l: LecturaConsumo): LecturaConsumo {
        persist.guardarLectura(l)
        return l
    }

    override fun listarPorMedidorMes(idMedidor: String, anio: Int, mes: Int): List<LecturaConsumo> {
        return persist.obtenerLecturas().filter {
            it.getIdMedidor() == idMedidor && it.getAnioLectura() == anio && it.getMesLectura() == mes
        }
    }

    override fun ultimaLectura(idMedidor: String): LecturaConsumo? {
        return persist.obtenerLecturas()
            .filter { it.getIdMedidor() == idMedidor }
            .maxByOrNull { it.getCreatedAt().time }
    }
}