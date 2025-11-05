package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor

interface MedidorRepositorio {
    fun crear(m: Medidor, rutCliente: String): Medidor
    fun listarPorCliente(rut: String): List<Medidor>
    fun obtenerPorCodigo(codigo: String): Medidor?
    fun eliminar(codigo: String): Boolean
}