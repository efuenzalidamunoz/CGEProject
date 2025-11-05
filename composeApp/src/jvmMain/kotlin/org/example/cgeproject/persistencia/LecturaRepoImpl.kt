package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

class LecturaRepoImpl: LecturaRepositorio {
    // Usa la base de datos central en memoria
    private val lecturas = PersistenciaDatos.lecturas

    override suspend fun registrar(l: LecturaConsumo): LecturaConsumo {
        lecturas.add(l)
        return l
    }

    override suspend fun listarPorMedidorMes(idMedidor: String, anio: Int, mes: Int): List<LecturaConsumo> {
        return lecturas.filter {
            it.getIdMedidor() == idMedidor && it.getAnioLectura() == anio && it.getMesLectura() == mes
        }
    }

    override suspend fun ultimaLectura(idMedidor: String): LecturaConsumo? {
        return lecturas
            .filter { it.getIdMedidor() == idMedidor }
            .sortedWith(compareByDescending<LecturaConsumo> { it.getAnioLectura() }.thenByDescending { it.getMesLectura() })
            .firstOrNull()
    }
}