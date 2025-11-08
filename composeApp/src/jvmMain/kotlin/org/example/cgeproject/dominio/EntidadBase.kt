package org.example.cgeproject.dominio

import java.util.Date

open class EntidadBase(
    private val id: String,
    private val createdAt: Date,
    private val updatedAt: Date
) {
    /** Getters **/
    fun getId(): String = id
    fun getCreatedAt(): Date = createdAt
    fun getUpdatedAt(): Date = updatedAt
}