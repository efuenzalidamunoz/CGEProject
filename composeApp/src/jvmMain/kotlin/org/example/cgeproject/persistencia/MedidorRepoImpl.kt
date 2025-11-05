package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico

class MedidorRepoImpl(private val persist: PersistenciaDatos) : MedidorRepositorio {

    override fun crear(m: Medidor, rutCliente: String): Medidor {
        // asociamos manualmente el rut del cliente
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

    override fun listarPorCliente(rut: String): List<Medidor> {
        return persist.obtenerMedidores().filter { it.getIdCliente() == rut }
    }

    override fun obtenerPorCodigo(codigo: String): Medidor? {
        return persist.buscarMedidorPorSerie(codigo)
    }

    override fun eliminar(codigo: String): Boolean {
        val medidor = persist.obtenerMedidores().find { it.getCodigo() == codigo }
        return medidor?.let { persist.eliminarMedidor(it.getId()) } ?: false
    }
}
