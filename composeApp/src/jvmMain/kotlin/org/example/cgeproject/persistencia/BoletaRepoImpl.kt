package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

class BoletaRepoImpl(private val persist: PersistenciaDatos): BoletaRepositorio {
    override fun guardar(b: Boleta): Boleta {
        persist.guardarBoleta(b)
        return b
    }

    override fun obtener(rut: String, anio: Int, mes: Int): Boleta? {
        return persist.obtenerBoletas().find {
            it.getIdCliente() == rut && it.getAnio() == anio && it.getMes() == mes
        }
    }

    override fun listarPorCliente(rut: String): List<Boleta> {
        return persist.obtenerBoletas().filter { it.getIdCliente() == rut }
    }

    override fun eliminarBoleta(rut: String, anio: Int, mes: Int) {
        persist.eliminarBoleta(rut, anio, mes)
    }
}