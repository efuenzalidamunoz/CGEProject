package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

class BoletaRepoImpl: BoletaRepositorio {
    // Usa la base de datos central en memoria
    override suspend fun guardar(b: Boleta): Boleta {
        TODO("Not yet implemented")
    }

    override suspend fun obtener(
        rut: String,
        anio: Int,
        mes: Int
    ): Boleta? {
        TODO("Not yet implemented")
    }

    override suspend fun listarPorCliente(rut: String): List<Boleta> {
        TODO("Not yet implemented")
    }

}