package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.persistencia.LecturaRepositorio
import org.example.cgeproject.persistencia.MedidorRepositorio

private enum class PantallaLectura {
    LISTA,
    FORMULARIO
}

class PantallaLecturas(private val lecturaRepositorio: LecturaRepositorio, private val medidorRepositorio: MedidorRepositorio) {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaLectura.LISTA) }
        var medidores by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            medidores = medidorRepositorio.listarPorCliente("") // Carga todos los medidores
        }

        when (pantallaActual) {
            PantallaLectura.LISTA -> {
                GestionLecturasContent(
                    onNavigateToForm = { pantallaActual = PantallaLectura.FORMULARIO }
                )
            }
            PantallaLectura.FORMULARIO -> {
                FormularioLecturaContent(
                    medidores = medidores,
                    onNavigateBack = { pantallaActual = PantallaLectura.LISTA },
                    onSave = { nuevaLectura ->
                        scope.launch {
                            lecturaRepositorio.registrar(nuevaLectura)
                            pantallaActual = PantallaLectura.LISTA
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionLecturasContent(onNavigateToForm: () -> Unit) {
        var idMedidor by remember { mutableStateOf("") }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var anio by remember { mutableStateOf(now.year.toString()) }
        var mes by remember { mutableStateOf(now.monthNumber.toString()) }
        val scope = rememberCoroutineScope()

        var ultimaLectura by remember { mutableStateOf<LecturaConsumo?>(null) }
        var lecturasDelMes by remember { mutableStateOf<List<LecturaConsumo>>(emptyList()) }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Lecturas de Consumo", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = blue)) },
            floatingActionButton = { FloatingActionButton(onClick = onNavigateToForm, containerColor = blue) { Text("+", color = Color.White, fontSize = 24.sp) } }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(paddingValues).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SearchCard(
                    idMedidor = idMedidor, onIdMedidorChange = { idMedidor = it },
                    anio = anio, onAnioChange = { anio = it },
                    mes = mes, onMesChange = { mes = it },
                    onSearch = {
                        scope.launch {
                            val anioInt = anio.toIntOrNull()
                            val mesInt = mes.toIntOrNull()
                            if (idMedidor.isNotBlank() && anioInt != null && mesInt != null) {
                                lecturasDelMes = lecturaRepositorio.listarPorMedidorMes(idMedidor, anioInt, mesInt)
                                ultimaLectura = lecturaRepositorio.ultimaLectura(idMedidor)
                            }
                        }
                    }
                )

                ultimaLectura?.let { LastReadingCard(it) }

                if (lecturasDelMes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ResultsTable(lecturasDelMes)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class) // Anotación añadida
    @Composable
    private fun FormularioLecturaContent(medidores: List<Medidor>, onNavigateBack: () -> Unit, onSave: (LecturaConsumo) -> Unit) {
        var medidorSeleccionado by remember { mutableStateOf<Medidor?>(null) }
        var anio by remember { mutableStateOf("") }
        var mes by remember { mutableStateOf("") }
        var consumo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        Column(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 50.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Registrar Nueva Lectura", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    MedidorDropDown(medidores = medidores, selectedMedidor = medidorSeleccionado, onMedidorSelected = { medidorSeleccionado = it })
                    OutlinedTextField(value = anio, onValueChange = { anio = it }, label = { Text("Año (YYYY)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mes, onValueChange = { mes = it }, label = { Text("Mes (1-12)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = consumo, onValueChange = { consumo = it }, label = { Text("Consumo (kWh)") }, modifier = Modifier.fillMaxWidth())

                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = {
                                val medidor = medidorSeleccionado
                                val anioInt = anio.toIntOrNull()
                                val mesInt = mes.toIntOrNull()
                                val consumoDouble = consumo.toDoubleOrNull()

                                if (medidor == null || anioInt == null || mesInt == null || consumoDouble == null) {
                                    error = "Datos inválidos. Revise los campos."
                                    return@Button
                                }
                                
                                val nuevaLectura = LecturaConsumo(
                                    id = "lec-${medidor.getCodigo()}-$anio-$mes",
                                    createdAt = Clock.System.now(),
                                    updatedAt = Clock.System.now(),
                                    idMedidor = medidor.getCodigo(),
                                    anio = anioInt,
                                    mes = mesInt,
                                    kwhLeidos = consumoDouble
                                )
                                onSave(nuevaLectura)
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
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MedidorDropDown(medidores: List<Medidor>, selectedMedidor: Medidor?, onMedidorSelected: (Medidor) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedMedidor?.getCodigo() ?: "Seleccione un medidor",
                onValueChange = {},
                readOnly = true,
                label = { Text("Medidor") },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.5f)) {
                medidores.forEach { medidor ->
                    DropdownMenuItem(text = { Text(medidor.getCodigo()) }, onClick = {
                        onMedidorSelected(medidor)
                        expanded = false
                    })
                }
            }
        }
    }

    @Composable
    private fun SearchCard(idMedidor: String, onIdMedidorChange: (String) -> Unit, anio: String, onAnioChange: (String) -> Unit, mes: String, onMesChange: (String) -> Unit, onSearch: () -> Unit) {
        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f)) {
            Column(modifier = Modifier.padding(30.dp)) {
                Text("Buscar Lecturas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = blue, modifier = Modifier.padding(bottom = 24.dp))
                OutlinedTextField(value = idMedidor, onValueChange = onIdMedidorChange, label = { Text("ID del Medidor") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = anio, onValueChange = onAnioChange, label = { Text("Año") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = mes, onValueChange = onMesChange, label = { Text("Mes") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onSearch, enabled = idMedidor.isNotBlank(), modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text("Buscar") }
            }
        }
    }

    private fun formatDisplayDate(lectura: LecturaConsumo): String {
        return "${lectura.getMesLectura().toString().padStart(2, '0')}/${lectura.getAnioLectura()}"
    }

    @Composable
    private fun LastReadingCard(lectura: LecturaConsumo) {
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Última Lectura Registrada", style = MaterialTheme.typography.titleLarge, color = blue)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Periodo: ${formatDisplayDate(lectura)}", style = MaterialTheme.typography.bodyLarge)
                Text("Consumo: ${lectura.getKwhLeidos()} kWh", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun ResultsTable(lecturas: List<LecturaConsumo>) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().background(blue.copy(alpha = 0.1f)).padding(12.dp)) {
                Text("Periodo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue)
                Text("Consumo (kWh)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = blue, textAlign = TextAlign.End)
            }
            HorizontalDivider()
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(lecturas) { lectura ->
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(formatDisplayDate(lectura), modifier = Modifier.weight(1f))
                        Text(lectura.getKwhLeidos().toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}