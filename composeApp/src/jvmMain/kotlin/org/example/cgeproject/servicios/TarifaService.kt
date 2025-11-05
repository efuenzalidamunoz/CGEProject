package org.example.cgeproject.servicios

import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.Tarifa
import org.example.cgeproject.dominio.TarifaComercial
import org.example.cgeproject.dominio.TarifaResidencial
import org.example.cgeproject.dominio.TipoTarifa

class TarifaService() {
    /**
     * Devuelve la tarifa aplicable para un cliente.
     * Determina la tarifa correcta según el cliente (residencial, comercial, etc.)
     */
    fun tarifaPara(cliente: Cliente): Tarifa {
        return when (cliente.getTipoTarifa()) {
            TipoTarifa.RESIDENCIAL -> {
                // Tarifa residencial con precios por tramos
                val tramos = mapOf(
                    200 to 120.5,    // Primeros 200 kWh a $120.5/kWh
                    400 to 150.8,  // Siguientes 200 kWh a $150.8/kWh
                    Int.MAX_VALUE to 180.2 // Excedente a $180.2/kWh
                )
                TarifaResidencial(
                    cargoFijo = 850.0,
                    preciosPorTramo = tramos
                )
            }
            TipoTarifa.COMERCIAL -> {
                // Tarifa comercial con precio único
                TarifaComercial(
                    cargoFijo = 2500.0,
                    precioKwh = 175.0,
                    recargoComercial = 0.05 // 5% de recargo sobre el subtotal + cargo fijo
                )
            }
        }
    }
}