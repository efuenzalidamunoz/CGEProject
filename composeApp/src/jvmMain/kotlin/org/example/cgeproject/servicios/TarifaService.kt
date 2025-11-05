package org.example.cgeproject.servicios

import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.Tarifa
import org.example.cgeproject.dominio.TarifaResidencial

class TarifaService {
    /**
     * Devuelve la tarifa aplicable para un cliente.
     * En esta versión, siempre devuelve una TarifaResidencial fija.
     */
    fun tarifaPara(cliente: Cliente): Tarifa {
        // Lógica de ejemplo: todos los clientes tienen la misma tarifa.
        return TarifaResidencial()
    }
}