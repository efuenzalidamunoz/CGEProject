package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor

class MedidorRepoImpl: MedidorRepositorio {
    // Usa la base de datos central en memoria
    override suspend fun crear(
        m: Medidor,
        rutCliente: String
    ): Medidor {
        TODO("Not yet implemented")
    }

    override suspend fun listarPorCliente(rut: String): List<Medidor> {
        TODO("Not yet implemented")
    }

    override suspend fun obtenerPorCodigo(codigo: String): Medidor? {
        TODO("Not yet implemented")
    }

    override suspend fun eliminar(codigo: String): Boolean {
        TODO("Not yet implemented")
    }


}