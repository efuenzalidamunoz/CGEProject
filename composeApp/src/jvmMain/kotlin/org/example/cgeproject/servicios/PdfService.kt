package org.example.cgeproject.servicios

import org.example.cgeproject.dominio.ExportablePDF

/**
 * Servicio para generar archivos PDF a partir de datos exportables.
 */
expect class PdfService() {

    fun generarPdf(exportable: ExportablePDF): ByteArray
}