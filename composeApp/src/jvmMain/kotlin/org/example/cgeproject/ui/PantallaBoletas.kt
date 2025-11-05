package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.persistencia.BoletaRepositorio
import org.example.cgeproject.servicios.BoletaService
import org.example.cgeproject.servicios.PdfService
import org.example.cgeproject.utils.saveFile

class PantallaBoletas(private val boletaService: BoletaService, private val boletaRepositorio: BoletaRepositorio, private val pdfService: PdfService) {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    @Composable
    fun PantallaPrincipal() {
        var rutCliente by remember { mutableStateOf("") }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var anio by remember { mutableStateOf(now.year.toString()) }
        var mes by remember { mutableStateOf(now.monthNumber.toString()) }

        var boletas by remember { mutableStateOf<List<Boleta>>(emptyList()) }
        var boletaSeleccionada by remember { mutableStateOf<Boleta?>(null) }
        var mensaje by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Generación y Consulta de Boletas", style = MaterialTheme.typography.headlineLarge, color = blue, modifier = Modifier.padding(vertical = 24.dp))

            ActionCard(
                rutCliente = rutCliente, onRutChange = { rutCliente = it },
                anio = anio, onAnioChange = { anio = it },
                mes = mes, onMesChange = { mes = it },
                onGenerate = {
                    scope.launch {
                        try {
                            boletaService.generarBoleta(rutCliente, anio.toInt(), mes.toInt())
                            mensaje = "Boleta para $rutCliente ($mes/$anio) generada con éxito."
                            boletas = boletaRepositorio.listarPorCliente(rutCliente)
                        } catch (e: Exception) { mensaje = "Error al generar boleta: ${e.message}" }
                    }
                },
                onSearch = {
                    scope.launch {
                        try {
                            boletas = boletaRepositorio.listarPorCliente(rutCliente)
                            mensaje = if (boletas.isEmpty()) "No se encontraron boletas para el cliente $rutCliente." else null
                        } catch (e: Exception) { mensaje = "Error al buscar boletas: ${e.message}" }
                    }
                }
            )

            mensaje?.let { Text(it, color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else blue, modifier = Modifier.padding(16.dp)) }

            if (boletas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                BoletasTable(boletas) { boleta -> boletaSeleccionada = boleta }
            }

            boletaSeleccionada?.let {
                DetalleBoletaDialog(
                    boleta = it,
                    onDismiss = { boletaSeleccionada = null },
                    onDownloadPdf = {
                        scope.launch {
                            try {
                                val pdfBytes = pdfService.generarPdf(it)
                                val fileName = "boleta_${it.getIdCliente()}_${it.getAnio()}_${it.getMes()}.pdf"
                                saveFile(fileName, pdfBytes)
                                mensaje = "PDF guardado como $fileName en tu directorio de usuario."
                            } catch (e: Exception) { mensaje = "Error al generar PDF: ${e.message}" }
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun ActionCard(
        rutCliente: String, onRutChange: (String) -> Unit,
        anio: String, onAnioChange: (String) -> Unit,
        mes: String, onMesChange: (String) -> Unit,
        onGenerate: () -> Unit,
        onSearch: () -> Unit
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(0.8f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Consultar Historial o Generar Nueva Boleta", style = MaterialTheme.typography.titleLarge, color = blue)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = rutCliente, onValueChange = onRutChange, label = { Text("RUT del Cliente") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                Text("Generar Boleta para un Periodo", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = anio, onValueChange = onAnioChange, label = { Text("Año") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = mes, onValueChange = onMesChange, label = { Text("Mes") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGenerate, enabled = rutCliente.isNotBlank(), modifier = Modifier.align(Alignment.End)) { Text("Generar Boleta") }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Consultar Historial de Boletas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onSearch, enabled = rutCliente.isNotBlank(), modifier = Modifier.align(Alignment.End)) { Text("Buscar Historial") }
            }
        }
    }

    @Composable
    private fun BoletasTable(boletas: List<Boleta>, onBoletaClick: (Boleta) -> Unit) {
        Column(modifier = Modifier.fillMaxWidth(0.8f)) {
            Row(modifier = Modifier.fillMaxWidth().background(blue.copy(alpha = 0.1f)).padding(12.dp)) {
                Text("Periodo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue)
                Text("Consumo (kWh)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue)
                Text("Total a Pagar", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue)
            }
            HorizontalDivider()

            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(boletas.sortedByDescending { it.getAnio() * 100 + it.getMes() }) { boleta ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp).clickable { onBoletaClick(boleta) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${boleta.getMes().toString().padStart(2, '0')}/${boleta.getAnio()}", modifier = Modifier.weight(1f))
                        Text(boleta.getKwhTotal().toString(), modifier = Modifier.weight(1f))
                        Text("\$${boleta.getDetalle().total}", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }

    @Composable
    private fun DetalleBoletaDialog(boleta: Boleta, onDismiss: () -> Unit, onDownloadPdf: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Detalle de la Boleta - ${boleta.getMes()}/${boleta.getAnio()}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetalleRow("Cliente:", boleta.getIdCliente())
                    DetalleRow("Consumo del Periodo:", "${boleta.getKwhTotal()} kWh")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetalleRow("Cargos Fijos:", "\$${boleta.getDetalle().cargos}")
                    DetalleRow("Costo Consumo:", "\$${boleta.getDetalle().subtotal - boleta.getDetalle().cargos}") // Cálculo del costo del consumo
                    DetalleRow("Subtotal:", "\$${boleta.getDetalle().subtotal}", isBold = true)
                    DetalleRow("IVA (19%):", "\$${boleta.getDetalle().iva}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetalleRow("TOTAL A PAGAR:", "\$${boleta.getDetalle().total}", isBold = true, isTotal = true)
                }
            },
            confirmButton = {
                Button(onClick = onDownloadPdf) { Text("Descargar PDF") }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cerrar") }
            }
        )
    }

    @Composable
    private fun DetalleRow(label: String, value: String, isBold: Boolean = false, isTotal: Boolean = false) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = if (isTotal) blue else Color.Unspecified)
        }
    }
}