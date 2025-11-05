package org.example.cgeproject.dominio

import org.example.cgeproject.servicios.PdfTable

interface ExportablePDF {
    fun toPdfTable(): PdfTable
}