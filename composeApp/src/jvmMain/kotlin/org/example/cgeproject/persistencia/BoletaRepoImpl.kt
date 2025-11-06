package org.example.cgeproject.persistencia

import org.example.cgeproject.dominio.Boleta

class BoletaRepoImpl(private val persist: PersistenciaDatos): BoletaRepositorio {
    /** Guardamos las boletas en la implementación del repositorio **/
    override fun guardar(b: Boleta): Boleta {
        persist.guardarBoleta(b)
        return b
    }

    /** Capturamos las boletas mediante el rut, año y mes de la facturación **/
    override fun obtener(rut: String, anio: Int, mes: Int): Boleta? {
        return persist.obtenerBoletas().find {
            it.getIdCliente() == rut && it.getAnio() == anio && it.getMes() == mes
        }
    }

    /** Listamos los clientes que se encuentren en el repositorio con los detalles de su boleta **/
    override fun listarPorCliente(rut: String): List<Boleta> {
        return persist.obtenerBoletas().filter { it.getIdCliente() == rut }
    }

    /** Se eliminan las boletas **/
    override fun eliminarBoleta(id: String) {
        persist.eliminarBoleta(id)
    }
}