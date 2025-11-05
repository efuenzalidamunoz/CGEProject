package org.example.cgeproject.dominio

class Operador(
    rut: String,
    nombre: String,
    email: String,
    private val perfil: String,
): Persona(rut, nombre, email) {
}