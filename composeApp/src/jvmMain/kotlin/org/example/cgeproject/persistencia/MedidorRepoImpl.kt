package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor

class MedidorRepoImpl: MedidorRepositorio {
    // Usa la base de datos central en memoria
    private val medidores = PersistenciaDatos.medidores

    override suspend fun crear(m: Medidor, rutCliente: String): Medidor {
        medidores.add(m)
        return m
    }

    override suspend fun listarPorCliente(rut: String): List<Medidor> {
        // CORREGIDO: Si el rut está en blanco, no se debe devolver nada.
        if (rut.isBlank()) return emptyList()
        return medidores.filter { it.getIdCliente() == rut }
    }

    override suspend fun obtenerPorCodigo(codigo: String): Medidor? {
        return medidores.find { it.getCodigo() == codigo }
    }

    override suspend fun eliminar(codigo: String): Boolean {
        val initialSize = medidores.size
        medidores.removeAll { it.getCodigo() == codigo }
        return medidores.size < initialSize
    }
}