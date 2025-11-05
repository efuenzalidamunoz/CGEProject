package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

interface BoletaRepositorio {
    suspend fun guardar(b: Boleta): Boleta
    suspend fun obtener(rut: String, anio: Int, mes: Int): Boleta?
    suspend fun listarPorCliente(rut: String): List<Boleta>
}