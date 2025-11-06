package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.LecturaConsumo

class LecturaRepoImpl(private val persist: PersistenciaDatos) : LecturaRepositorio {

    /** Registramos las lecturas en el repositorio **/
    override fun registrar(l: LecturaConsumo): LecturaConsumo {
        persist.guardarLectura(l)
        return l
    }

    /** Listamos las lecturas de un medidor en un mes y año específicos **/
    override fun listarPorMedidorMes(idMedidor: String, anio: Int, mes: Int): List<LecturaConsumo> {
        return persist.obtenerLecturas().filter {
            it.getIdMedidor() == idMedidor && it.getAnioLectura() == anio && it.getMesLectura() == mes
        }
    }

    /** Captura la ultima lectura hecha por un medidior **/
    override fun ultimaLectura(idMedidor: String): LecturaConsumo? {
        return persist.obtenerLecturas()
            .filter { it.getIdMedidor() == idMedidor }
            .maxByOrNull { it.getCreatedAt().time }
    }

    /** Eliminamos las lecturas **/
    override fun eliminarLectura(id: String): Boolean {
        return persist.eliminarLectura(id)
    }

}