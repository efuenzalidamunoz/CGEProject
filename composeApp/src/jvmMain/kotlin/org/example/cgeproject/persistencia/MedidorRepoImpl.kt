package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor

class MedidorRepoImpl: MedidorRepositorio {
    override fun crear(
        m: Medidor,
        rutCliente: String
    ): Medidor {
        TODO("Not yet implemented")
    }

    override fun listarPorCliente(rut: String): List<Medidor> {
        TODO("Not yet implemented")
    }

    override fun obtenerPorCodigo(codigo: String): Medidor? {
        TODO("Not yet implemented")
    }

    override fun eliminar(codigo: String): Boolean {
        TODO("Not yet implemented")
    }


}