package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.Medidor


object PersistenciaDatos {
    val medidores = mutableListOf<Medidor>()
    val lecturas = mutableListOf<LecturaConsumo>()
    val boletas = mutableListOf<Boleta>()
}
