package org.example.cgeproject.dominio

open class Persona(
    private var rut: String,
    private var nombre: String,
    private var email: String,
) {
    /** Getters **/
    fun getRut(): String = rut
    fun getNombre(): String = nombre
    fun getEmail():String = email
}
