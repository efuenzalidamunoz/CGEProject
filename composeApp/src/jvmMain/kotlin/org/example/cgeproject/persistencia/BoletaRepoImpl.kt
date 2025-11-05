package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

class BoletaRepoImpl: BoletaRepositorio {
    // Usa la base de datos central en memoria
    private val boletas = PersistenciaDatos.boletas

    override suspend fun guardar(b: Boleta): Boleta {
        val iterator = boletas.iterator()
        while (iterator.hasNext()) {
            val boleta = iterator.next()
            if (boleta.getIdCliente() == b.getIdCliente() && boleta.getAnio() == b.getAnio() && boleta.getMes() == b.getMes()) {
                iterator.remove()
                break
            }
        }
        boletas.add(b)
        return b
    }

    override suspend fun obtener(rut: String, anio: Int, mes: Int): Boleta? {
        return boletas.find { it.getIdCliente() == rut && it.getAnio() == anio && it.getMes() == mes }
    }

    override suspend fun listarPorCliente(rut: String): List<Boleta> {
        return boletas.filter { it.getIdCliente() == rut }
    }
}