package org.example.cgeproject.dominio

open class Persona(
    private val rut: String,
    private val nombre: String,
    private val email: String,
) {
    fun getRut(): String = rut
    fun getNombre(): String = nombre
    fun getEmail():String = email
}
