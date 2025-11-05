package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor

interface MedidorRepositorio {
    suspend fun crear(m: Medidor, rutCliente: String): Medidor
    suspend fun listarPorCliente(rut: String): List<Medidor>
    suspend fun obtenerPorCodigo(codigo: String): Medidor?
    suspend fun eliminar(codigo: String): Boolean
}