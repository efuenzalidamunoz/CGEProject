package org.example.cgeproject.servicios

import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.Cliente

/**
 * Servicio para generar archivos PDF a partir de datos exportables.
 */
class PdfService() {

    fun generarPdf(boletas: List<Boleta>, clientes: Map<String, Cliente>): ByteArray {
        TODO("Implementar la generación de PDF utilizando una biblioteca adecuada")
    }
}