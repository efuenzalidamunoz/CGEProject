package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Cliente

class ClienteRepoImpl(private val persist: PersistenciaDatos) : ClienteRepositorio {
    override fun crear(c: Cliente): Cliente {
        persist.guardarCliente(c)
        return c
    }

    override fun actualizar(c: Cliente): Cliente {
        // Eliminamos el cliente anterior y volvemos a guardar
        persist.eliminarCliente(c.getRut())
        persist.guardarCliente(c)
        return c
    }

    override fun eliminar(rut: String): Boolean {
        return persist.eliminarCliente(rut)
    }

    override fun obtenerPorRut(rut: String): Cliente? {
        return persist.buscarClientePorRut(rut)
    }

    override fun listar(filtro: String): List<Cliente> {
        val lista = persist.obtenerClientes()
        return if (filtro.isBlank()) lista
        else lista.filter {
            it.getNombre().contains(filtro, ignoreCase = true) ||
                    it.getRut().contains(filtro, ignoreCase = true)
        }
    }
}