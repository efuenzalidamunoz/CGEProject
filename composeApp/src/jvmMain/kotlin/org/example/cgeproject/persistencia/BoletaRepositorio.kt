package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

interface BoletaRepositorio {
    fun guardar(b: Boleta): Boleta
    fun obtener(rut: String, anio: Int, mes: Int): Boleta?
    fun listarPorCliente(rut: String): List<Boleta>
}