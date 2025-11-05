package org.example.cgeproject.servicios

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.Cliente
import java.io.ByteArrayOutputStream

class PdfService() {

    fun generarPdf(boletas: List<Boleta>, clientes: Map<String, Cliente>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        try {
            for (boleta in boletas) {
                val cliente = clientes[boleta.getIdCliente()] ?: throw Exception("Cliente no encontrado para la boleta")
                val pdfTableData = boleta.toPdfTable()

                document.add(Paragraph("Boleta CGE").setBold().setFontSize(18f))
                document.add(Paragraph("Cliente: ${cliente.getNombre()}"))
                document.add(Paragraph("RUT: ${cliente.getRut()}"))
                document.add(Paragraph("Dirección: ${cliente.getDireccionFacturacion()}"))
                document.add(Paragraph("Número de cliente: ${cliente.getRut()}"))
                document.add(Paragraph("Fecha de emisión: ${boleta.getAnio()}/${boleta.getMes()}"))
                document.add(Paragraph("\n")) // Espacio

                val table = Table(pdfTableData.headers.size)
                pdfTableData.headers.forEach { header ->
                    table.addHeaderCell(Paragraph(header).setBold())
                }
                pdfTableData.rows.forEach { row ->
                    row.forEach { cell ->
                        table.addCell(Paragraph(cell))
                    }
                }
                document.add(table)
                document.add(Paragraph("\n\n"))
            }
        } finally {
            document.close()
        }

        return outputStream.toByteArray()
    }
}