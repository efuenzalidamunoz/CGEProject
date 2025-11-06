package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Cliente

class ClienteRepoImpl(private val persist: PersistenciaDatos) : ClienteRepositorio {
    /** Creamos a los clientes y los guardamos en el repositorio **/
    override fun crear(c: Cliente): Cliente {
        persist.guardarCliente(c)
        return c
    }

    /** Actualizamos las lista **/
    override fun actualizar(c: Cliente): Cliente {
        // Eliminamos el cliente anterior y volvemos a guardar
        persist.eliminarCliente(c.getRut())
        persist.guardarCliente(c)
        return c
    }

    /** Eliminamos a los clientes del repositorio **/
    override fun eliminar(rut: String): Boolean {
        return persist.eliminarCliente(rut)
    }

    /** Buscamos al cliente por su rut **/
    override fun obtenerPorRut(rut: String): Cliente? {
        return persist.buscarClientePorRut(rut)
    }

    /** Enlistamos a los clientes **/
    override fun listar(filtro: String): List<Cliente> {
        val lista = persist.obtenerClientes()
        return if (filtro.isBlank()) lista
        else lista.filter {
            it.getNombre().contains(filtro, ignoreCase = true) ||
                    it.getRut().contains(filtro, ignoreCase = true)
        }
    }
}