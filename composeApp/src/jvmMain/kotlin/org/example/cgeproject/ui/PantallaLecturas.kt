package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- Modelo de Dominio (Asumido) con fecha como String ---
// TODO: Reemplazar con tu clase de dominio real.
data class LecturaConsumo(
    val idMedidor: String,
    val fecha: String, // Cambiado a String
    val consumoKwh: Int
)

// --- Enum para Navegación ---
private enum class PantallaLectura {
    LISTA,
    FORMULARIO
}

class PantallaLecturas {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    // --- Datos de Ejemplo con fecha como String ---
    // TODO: Reemplazar con las llamadas a tu repositorio.
    private val lecturasDeEjemplo = listOf(
        LecturaConsumo("MED-001", "2024-10-05", 150),
        LecturaConsumo("MED-001", "2024-10-12", 155),
        LecturaConsumo("MED-001", "2024-09-28", 140),
        LecturaConsumo("MED-002", "2024-10-08", 320)
    )

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaLectura.LISTA) }

        when (pantallaActual) {
            PantallaLectura.LISTA -> {
                GestionLecturasContent(
                    onNavigateToForm = { pantallaActual = PantallaLectura.FORMULARIO }
                )
            }

            PantallaLectura.FORMULARIO -> {
                FormularioLecturaContent(
                    onNavigateBack = { pantallaActual = PantallaLectura.LISTA },
                    onSave = { nuevaLectura ->
                        // TODO: Implementar lógica de 'registrar(l: LecturaConsumo)'
                        println("Guardando nueva lectura: $nuevaLectura")
                        pantallaActual = PantallaLectura.LISTA
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionLecturasContent(onNavigateToForm: () -> Unit) {
        var idMedidor by remember { mutableStateOf("") }
        var anio by remember { mutableStateOf("2024") }
        var mes by remember { mutableStateOf("10") }

        var ultimaLectura by remember { mutableStateOf<LecturaConsumo?>(null) }
        var lecturasDelMes by remember { mutableStateOf<List<LecturaConsumo>>(emptyList()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lecturas de Consumo", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = blue)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onNavigateToForm, containerColor = blue) {
                    Text("+", color = Color.White, fontSize = 24.sp)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Formulario de Búsqueda
                SearchCard(
                    idMedidor = idMedidor, onIdMedidorChange = { idMedidor = it },
                    anio = anio, onAnioChange = { anio = it },
                    mes = mes, onMesChange = { mes = it },
                    onSearch = {
                        // TODO: Implementar lógica de 'listarPorMedidorMes' y 'ultimaLectura'
                        val anioInt = anio.toIntOrNull()
                        val mesInt = mes.toIntOrNull()
                        if (idMedidor.isNotBlank() && anioInt != null && mesInt != null) {
                            val mesStr = mesInt.toString().padStart(2, '0')
                            lecturasDelMes = lecturasDeEjemplo.filter {
                                it.idMedidor == idMedidor && it.fecha.startsWith("$anioInt-$mesStr")
                            }
                            // maxByOrNull sigue funcionando con el formato "YYYY-MM-DD"
                            ultimaLectura = lecturasDeEjemplo
                                .filter { it.idMedidor == idMedidor }
                                .maxByOrNull { it.fecha }
                        }
                    }
                )

                // Mostrar Última Lectura
                ultimaLectura?.let {
                    LastReadingCard(it)
                }

                // Tabla de Resultados
                if (lecturasDelMes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ResultsTable(lecturasDelMes)
                }
            }
        }
    }

    @Composable
    private fun FormularioLecturaContent(onNavigateBack: () -> Unit, onSave: (LecturaConsumo) -> Unit) {
        var idMedidor by remember { mutableStateOf("") }
        // La fecha se obtiene como String directamente
        var fecha by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
        var consumo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 50.dp)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Registrar Nueva Lectura", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = idMedidor,
                        onValueChange = { idMedidor = it },
                        label = { Text("ID Medidor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = consumo,
                        onValueChange = { consumo = it },
                        label = { Text("Consumo (kWh)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    error?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = {
                                val consumoInt = consumo.toIntOrNull()
                                // Validación simple de String
                                if (idMedidor.isBlank() || fecha.isBlank() || consumoInt == null) {
                                    error = "Datos inválidos. Revise los campos."
                                    return@Button
                                }
                                onSave(LecturaConsumo(idMedidor, fecha, consumoInt))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = blue)
                        ) { Text("Guardar Lectura") }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = onNavigateBack) { Text("Cancelar") }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchCard(
        idMedidor: String, onIdMedidorChange: (String) -> Unit,
        anio: String, onAnioChange: (String) -> Unit,
        mes: String, onMesChange: (String) -> Unit,
        onSearch: () -> Unit
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Buscar Lecturas", style = MaterialTheme.typography.titleLarge, color = blue)
                OutlinedTextField(
                    value = idMedidor,
                    onValueChange = onIdMedidorChange,
                    label = { Text("ID del Medidor") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = anio,
                        onValueChange = onAnioChange,
                        label = { Text("Año") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mes,
                        onValueChange = onMesChange,
                        label = { Text("Mes") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onSearch, enabled = idMedidor.isNotBlank(), modifier = Modifier.align(Alignment.End)) {
                    Text("Buscar")
                }
            }
        }
    }

    // Función de ayuda para formatear el string de fecha
    private fun formatDisplayDate(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else dateStr
        } catch (e: Exception) {
            dateStr // Devuelve el original si el formato es inesperado
        }
    }

    @Composable
    private fun LastReadingCard(lectura: LecturaConsumo) {
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Última Lectura Registrada", style = MaterialTheme.typography.titleLarge, color = blue)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Fecha: ${formatDisplayDate(lectura.fecha)}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Consumo: ${lectura.consumoKwh} kWh",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun ResultsTable(lecturas: List<LecturaConsumo>) {
        Column {
            // Encabezado de la tabla
            Row(modifier = Modifier.fillMaxWidth().background(blue.copy(alpha = 0.1f)).padding(12.dp)) {
                Text("Fecha", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue)
                Text(
                    "Consumo (kWh)",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    color = blue,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider()
            // Filas de la tabla
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(lecturas) { lectura ->
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(formatDisplayDate(lectura.fecha), modifier = Modifier.weight(1f))
                        Text(lectura.consumoKwh.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}