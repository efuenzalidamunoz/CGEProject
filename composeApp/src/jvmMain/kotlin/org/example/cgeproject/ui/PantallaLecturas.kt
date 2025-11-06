package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.persistencia.FileSystemStorageDriver
import org.example.cgeproject.persistencia.LecturaRepoImpl
import org.example.cgeproject.persistencia.PersistenciaDatos
import org.example.cgeproject.persistencia.ClienteRepoImpl
import org.example.cgeproject.persistencia.MedidorRepoImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


// --- Enum para Navegación ---
private enum class PantallaLectura {
    LISTA,
    FORMULARIO
}

class PantallaLecturas {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    // --- Repositorios ---
    private val repo = LecturaRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))
    private val clienteRepo = ClienteRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))
    private val medidorRepo = MedidorRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))

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
                        repo.registrar(nuevaLectura)
                        pantallaActual = PantallaLectura.LISTA
                    },
                    clienteRepo = clienteRepo,
                    medidorRepo = medidorRepo
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionLecturasContent(onNavigateToForm: () -> Unit) {
        var idMedidor by remember { mutableStateOf("") }
        var anio by remember { mutableStateOf("") }
        var mes by remember { mutableStateOf("") }

        var ultimaLectura by remember { mutableStateOf<LecturaConsumo?>(null) }
        var lecturasDelMes by remember { mutableStateOf<List<LecturaConsumo>>(emptyList()) }
        var error by remember { mutableStateOf<String?>(null) }

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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Formulario de Búsqueda
                SearchCard(
                    idMedidor = idMedidor, onIdMedidorChange = { idMedidor = it },
                    anio = anio, onAnioChange = { anio = it },
                    mes = mes, onMesChange = { mes = it },
                    onSearch = {
                        val anioInt = anio.toIntOrNull()
                        val mesInt = mes.toIntOrNull()
                        if (idMedidor.isNotBlank() && anioInt != null && mesInt != null) {
                            lecturasDelMes = repo.listarPorMedidorMes(idMedidor, anioInt, mesInt)
                            ultimaLectura = repo.ultimaLectura(idMedidor)

                            if (lecturasDelMes.isEmpty() && ultimaLectura == null) {
                                error = "No existe un medidor con ese ID"
                            } else {
                                error = null
                            }

                        } else {
                            error = "Complete todos los campos correctamente."
                        }
                    },
                    searchError = error
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FormularioLecturaContent(
        onNavigateBack: () -> Unit,
        onSave: (LecturaConsumo) -> Unit,
        clienteRepo: ClienteRepoImpl,
        medidorRepo: MedidorRepoImpl
    ) {
        var rutCliente by remember { mutableStateOf("") }
        var medidoresCliente by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        var selectedMedidor by remember { mutableStateOf<Medidor?>(null) }
        var expandedMedidorDropdown by remember { mutableStateOf(false) }

        var anio by remember { mutableStateOf("") }
        var mes by remember { mutableStateOf("") }
        var consumo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        // Efecto para cargar medidores cuando el RUT del cliente cambia
        LaunchedEffect(rutCliente) {
            if (rutCliente.isNotBlank()) {
                try {
                    val cliente = clienteRepo.obtenerPorRut(rutCliente)
                    if (cliente != null) {
                        medidoresCliente = medidorRepo.listarPorCliente(rutCliente)
                        selectedMedidor = medidoresCliente.firstOrNull()
                        error = null
                    } else {
                        medidoresCliente = emptyList()
                        selectedMedidor = null
                        error = "Cliente no encontrado."
                    }
                } catch (e: Exception) {
                    medidoresCliente = emptyList()
                    selectedMedidor = null
                    error = e.message ?: "Error al buscar cliente o medidores."
                }
            } else {
                medidoresCliente = emptyList()
                selectedMedidor = null
                error = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Registrar Nueva Lectura", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = rutCliente,
                        onValueChange = { rutCliente = it },
                        label = { Text("RUT Cliente") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Medidor
                    ExposedDropdownMenuBox(
                        expanded = expandedMedidorDropdown,
                        onExpandedChange = { expandedMedidorDropdown = !expandedMedidorDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMedidor?.getCodigo() ?: "Seleccione un medidor",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Medidor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedidorDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedMedidorDropdown,
                            onDismissRequest = { expandedMedidorDropdown = false }
                        ) {
                            medidoresCliente.forEach { medidor ->
                                DropdownMenuItem(text = { Text(medidor.getCodigo()) }, onClick = {
                                    selectedMedidor = medidor
                                    expandedMedidorDropdown = false
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = anio,
                            onValueChange = { anio = it },
                            label = { Text("Año") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mes,
                            onValueChange = { mes = it },
                            label = { Text("Mes") },
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                                val anioInt = anio.toIntOrNull()
                                val mesInt = mes.toIntOrNull()
                                val consumoDouble = consumo.toDoubleOrNull()

                                if (rutCliente.isBlank() || selectedMedidor == null || anioInt == null || mesInt == null || consumoDouble == null) {
                                    error = "Todos los campos son obligatorios y deben ser válidos."
                                    return@Button
                                }
                                val now = Date()
                                onSave(
                                    LecturaConsumo(
                                        id = UUID.randomUUID().toString(),
                                        createdAt = now,
                                        updatedAt = now,
                                        idMedidor = selectedMedidor!!.getCodigo(), // Usamos el medidor seleccionado
                                        anio = anioInt,
                                        mes = mesInt,
                                        kwhLeidos = consumoDouble
                                    )
                                )
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
        onSearch: () -> Unit,
        searchError: String?
    ) {


        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
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
                searchError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(onClick = onSearch, enabled = idMedidor.isNotBlank(), modifier = Modifier.align(Alignment.End)) {
                    Text("Buscar")
                }
            }
        }
    }

    private fun formatDisplayDate(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(date)
    }

    @Composable
    private fun LastReadingCard(lectura: LecturaConsumo) {
        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Última Lectura Registrada", style = MaterialTheme.typography.titleLarge, color = blue)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Fecha: ${formatDisplayDate(lectura.getCreatedAt())}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Consumo: ${lectura.getKwhLeidos()} kWh",
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
                items(lecturas.sortedByDescending { it.getCreatedAt() }) { lectura ->
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(formatDisplayDate(lectura.getCreatedAt()), modifier = Modifier.weight(1f))
                        Text(lectura.getKwhLeidos().toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
