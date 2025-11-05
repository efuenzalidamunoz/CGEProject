// en: shared/src/commonMain/kotlin/org/example/cgeproject/persistencia/ClienteDto.kt
package org.example.cgeproject.persistencia

import kotlinx.serialization.Serializable

@Serializable
internal data class ClienteDto(
    val rut: String,
    val nombre: String,
    val email: String,
    val direccion: String,
    val estado: String
)