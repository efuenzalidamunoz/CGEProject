package org.example.cgeproject.dominio

import java.util.Date


open class EntidadBase(
    private val id: String,
    private val createdAt: Date, // Tipo corregido
    private val updatedAt: Date  // Tipo corregido
) {
    fun getId(): String = id
    fun getCreatedAt(): Date = createdAt
    fun getUpdatedAt(): Date = updatedAt
}