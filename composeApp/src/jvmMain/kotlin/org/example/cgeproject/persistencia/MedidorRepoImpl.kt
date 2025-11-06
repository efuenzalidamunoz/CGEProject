package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico

class MedidorRepoImpl(private val persist: PersistenciaDatos) : MedidorRepositorio {

    /** Crea un nuevo medidor **/
    override fun crear(m: Medidor, rutCliente: String): Medidor {
        val medidorConCliente = when (m) {
            is MedidorMonofasico -> MedidorMonofasico(
                m.getId(), m.getCreatedAt(), m.getUpdatedAt(),
                m.getCodigo(), m.getDireccionSuministro(),
                m.getActivo(), rutCliente, m.getPotenciaMaxKw()
            )
            is MedidorTrifasico -> MedidorTrifasico(
                m.getId(), m.getCreatedAt(), m.getUpdatedAt(),
                m.getCodigo(), m.getDireccionSuministro(),
                m.getActivo(), rutCliente, m.getPotenciaMaxKw(), m.getFactorPotencia()
            )
            else -> m
        }
        persist.guardarMedidor(medidorConCliente)
        return medidorConCliente
    }

    /** Lisra los medidores asociados a clientes */
    override fun listarPorCliente(rut: String): List<Medidor> {
        return persist.obtenerMedidores().filter { it.getIdCliente() == rut }
    }

    /** Captura los medidores por su codigo **/
    override fun obtenerPorCodigo(codigo: String): Medidor? {
        return persist.buscarMedidorPorSerie(codigo)
    }

    /** Elimina los medidores **/
    override fun eliminar(codigo: String): Boolean {
        val medidor = persist.obtenerMedidores().find { it.getCodigo() == codigo }
        return medidor?.let { persist.eliminarMedidor(it.getId()) } ?: false
    }
}
