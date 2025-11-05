package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.EstadoCliente

/**
 * Funciones de extensión para mapear entre el dominio y los DTOs.
 * El modificador 'internal' asegura que solo son visibles dentro de este módulo.
 */

internal fun Cliente.toDto(): ClienteDto {
    return ClienteDto(
        rut = this.getRut(),
        nombre = this.getNombre(),
        email = this.getEmail(),
        direccion = this.getDireccionFacturacion(),
        estado = this.getEstado().name
    )
}

internal fun ClienteDto.toDomain(): Cliente {
    return Cliente(
        rut = this.rut,
        nombre = this.nombre,
        email = this.email,
        direccionFacturacion = this.direccion,
        estado = EstadoCliente.valueOf(this.estado)
    )
}