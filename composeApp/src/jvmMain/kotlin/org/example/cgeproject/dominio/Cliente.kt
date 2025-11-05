package org.example.cgeproject.dominio
import androidx.compose.runtime.mutableStateListOf

class Cliente(
    rut: String,
    nombre: String,
    email: String,
    private var direccionFacturacion: String,
    private var estado: EstadoCliente,
    private var boletas: MutableList<Boleta> = mutableStateListOf(),
    private var medidores: MutableList<Medidor> = mutableStateListOf()
): Persona(rut, nombre, email) {

    fun getDireccionFacturacion(): String = direccionFacturacion
    fun getEstado(): EstadoCliente = estado

    fun agregarBoleta(boleta: Boleta) { boletas.add(boleta) }
    fun agregarMedidor(medidor: Medidor) { medidores.add(medidor) }

}