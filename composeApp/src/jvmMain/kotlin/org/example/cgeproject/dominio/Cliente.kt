package org.example.cgeproject.dominio
import androidx.compose.runtime.mutableStateListOf


enum class TipoTarifa { RESIDENCIAL, COMERCIAL }

class Cliente(
    rut: String,
    nombre: String,
    email: String,
    private var direccionFacturacion: String,
    private var estado: EstadoCliente,
    private var tipoTarifa: TipoTarifa,
    private var boletas: MutableList<Boleta> = mutableStateListOf(),
    private var medidores: MutableList<Medidor> = mutableStateListOf()
): Persona(rut, nombre, email) {

    /** Getters **/
    fun getDireccionFacturacion(): String = direccionFacturacion
    fun getEstado(): EstadoCliente = estado
    fun getTipoTarifa(): TipoTarifa = tipoTarifa

    /** Agregamos boletas y medidores **/
    fun agregarBoleta(boleta: Boleta) { boletas.add(boleta) }
    fun agregarMedidor(medidor: Medidor) { medidores.add(medidor) }
}