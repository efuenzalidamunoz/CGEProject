package org.example.cgeproject.dominio

import java.util.Date

/**
 * Clase base para todas las entidades del dominio.
 */
open class EntidadBase(
    val id: String,
    val createdAt: Date, // Tipo corregido
    val updatedAt: Date  // Tipo corregido
)