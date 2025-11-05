package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

class BoletaRepoImpl: BoletaRepositorio {
    override fun guardar(b: Boleta): Boleta {
        TODO("Not yet implemented")
    }

    override fun obtener(
        rut: String,
        anio: Int,
        mes: Int
    ): Boleta? {
        TODO("Not yet implemented")
    }

    override fun listarPorCliente(rut: String): List<Boleta> {
        TODO("Not yet implemented")
    }

}