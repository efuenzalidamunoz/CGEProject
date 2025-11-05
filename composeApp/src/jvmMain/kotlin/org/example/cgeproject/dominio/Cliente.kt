package org.example.cgeproject.dominio
import androidx.compose.runtime.mutableStateListOf

class Cliente(
    rut: String,
    nombre: String,
    email: String,
    private val direccionFacturacion: String,
    private val estado: EstadoCliente,
    private val boletas: MutableList<Boleta> = mutableStateListOf(),
    private val medidores: MutableList<Medidor> = mutableStateListOf()
): Persona(rut, nombre, email) {

    fun getDireccionFacturacion(): String = direccionFacturacion
    fun getEstado(): EstadoCliente = estado

    fun agregarBoleta(boleta: Boleta) { boletas.add(boleta) }
    fun agregarMedidor(medidor: Medidor) { medidores.add(medidor) }

}