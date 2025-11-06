package org.example.cgeproject.servicios

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.Cliente
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class PdfService() {

    private val primaryColor = DeviceRgb(0, 22, 137) // Azul oscuro
    private val accentColor = DeviceRgb(0, 123, 255) // Azul más claro
    private val headerBgColor = DeviceRgb(230, 230, 230) // Gris claro para encabezados

    fun generarPdf(boletas: List<Boleta>, clientes: Map<String, Cliente>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.setMargins(50f, 50f, 50f, 50f)

        /** Este es el formato del PDF **/
        try {
            for (boleta in boletas) {
                val cliente = clientes[boleta.getIdCliente()] ?: throw Exception("Cliente no encontrado para la boleta")
                val pdfTableData = boleta.toPdfTable()

                // Título principal
                document.add(
                    Paragraph("Boleta de Consumo Eléctrico")
                        .setFontSize(24f)
                        .setBold()
                        .setFontColor(primaryColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20f)
                )

                // Información del cliente
                document.add(
                    Paragraph("Datos del Cliente")
                        .setFontSize(16f)
                        .setBold()
                        .setFontColor(accentColor)
                        .setMarginBottom(10f)
                )
                document.add(Paragraph("Nombre: ${cliente.getNombre()}"))
                document.add(Paragraph("RUT: ${cliente.getRut()}"))
                document.add(Paragraph("Dirección: ${cliente.getDireccionFacturacion()}"))
                document.add(Paragraph("Número de Cliente: ${cliente.getRut()}"))
                document.add(Paragraph("Tipo de Tarifa: ${cliente.getTipoTarifa()}"))
                document.add(Paragraph("Estado: ${cliente.getEstado()}"))
                document.add(Paragraph("").setMarginBottom(20f))

                // Información de la boleta
                document.add(
                    Paragraph("Detalles de la Boleta")
                        .setFontSize(16f)
                        .setBold()
                        .setFontColor(accentColor)
                        .setMarginBottom(10f)
                )
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                document.add(Paragraph("Fecha de Emisión: ${dateFormat.format(Date())}")) // Fecha actual de generación del PDF
                document.add(Paragraph("Período Facturado: ${boleta.getMes()}/${boleta.getAnio()}"))
                document.add(Paragraph("Consumo Total (kWh): ${"%.2f".format(boleta.getKwhTotal())}"))
                document.add(Paragraph("Estado de la Boleta: ${boleta.getEstado()}"))
                document.add(Paragraph("").setMarginBottom(20f)) // Espacio

                // Tabla de detalles de la boleta
                val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f))) // Dos columnas, 30% y 70%
                    .useAllAvailableWidth()
                    .setMarginBottom(20f)

                // Encabezados de la tabla
                table.addHeaderCell(
                    Cell().add(Paragraph("Concepto").setBold().setFontColor(primaryColor))
                        .setBackgroundColor(headerBgColor)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(SolidBorder(primaryColor, 0.5f))
                        .setPadding(5f)
                )
                table.addHeaderCell(
                    Cell().add(Paragraph("Monto").setBold().setFontColor(primaryColor))
                        .setBackgroundColor(headerBgColor)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(SolidBorder(primaryColor, 0.5f))
                        .setPadding(5f)
                )

                // Filas de la tabla
                val detalle = boleta.getDetalle()
                table.addCell(createCell("Subtotal:", TextAlignment.LEFT))
                table.addCell(createCell("$ ${"%.0f".format(detalle.subtotal)}", TextAlignment.RIGHT))

                table.addCell(createCell("Cargos Adicionales:", TextAlignment.LEFT))
                table.addCell(createCell("$ ${"%.0f".format(detalle.cargos)}", TextAlignment.RIGHT))

                table.addCell(createCell("IVA (19%):", TextAlignment.LEFT))
                table.addCell(createCell("$ ${"%.0f".format(detalle.iva)}", TextAlignment.RIGHT))

                // Fila de total
                table.addCell(
                    Cell().add(Paragraph("Total a Pagar").setBold().setFontSize(14f).setFontColor(primaryColor))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(SolidBorder(primaryColor, 0.5f))
                        .setPadding(5f)
                )
                table.addCell(
                    Cell().add(Paragraph("$ ${"%.0f".format(detalle.total)}").setBold().setFontSize(14f).setFontColor(primaryColor))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(SolidBorder(primaryColor, 0.5f))
                        .setPadding(5f)
                )

                document.add(table)

                // Mensaje final
                document.add(
                    Paragraph("Gracias por preferir CGE.")
                        .setFontSize(12f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(30f)
                        .setFontColor(primaryColor)
                )

                if (boletas.indexOf(boleta) < boletas.size - 1) {
                    document.add(Paragraph("").setMarginBottom(50f))
                    document.add(Paragraph("--- Nueva Boleta ---").setTextAlignment(TextAlignment.CENTER).setFontColor(DeviceRgb(150,150,150)).setMarginBottom(50f))
                }
            }
        } finally {
            document.close()
        }

        return outputStream.toByteArray()
    }

    private fun createCell(content: String, alignment: TextAlignment): Cell {
        return Cell().add(Paragraph(content))
            .setTextAlignment(alignment)
            .setBorder(SolidBorder(DeviceRgb(200, 200, 200), 0.2f)) // Borde más sutil
            .setPadding(5f)
    }
}
