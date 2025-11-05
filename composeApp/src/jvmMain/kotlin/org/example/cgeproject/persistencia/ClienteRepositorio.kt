package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Cliente

interface ClienteRepositorio {
    suspend fun crear(c: Cliente): Cliente
    suspend fun actualizar(c: Cliente): Cliente
    suspend fun eliminar(rut: String): Boolean
    suspend fun obtenerPorRut(rut: String): Cliente?
    suspend fun listar(filtro: String = ""): List<Cliente>
}