package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import java.util.Calendar
import java.util.Date


// Enum para Navegación
private enum class PantallaLectura {
    LISTA,
    FORMULARIO
}

class PantallaLecturas {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    private val repo = LecturaRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))
    private val clienteRepo = ClienteRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))
    private val medidorRepo = MedidorRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))

    @Composable
    /** Selecciona entre pantallas y nos ayuda en la navegación de las
     * pantallas secundarias
     * **/
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

    @Composable
    /**
     * Sección de encabezado para la pantalla de lecturas.
     * Muestra un título y una descripción en un fondo azul.
     */
    private fun HeaderSection() {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(blue)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().padding(horizontal = 100.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Lecturas de Consumo", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Registra y consulta el historial de lecturas de consumo de los medidores.",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    /** Esta funcion muestra la tabla con las lecturas de un medidor asociado **/
    private fun GestionLecturasContent(onNavigateToForm: () -> Unit) {
        var anio by remember { mutableStateOf("") }
        var mes by remember { mutableStateOf<String?>(null) }

        var rutClienteBusqueda by remember { mutableStateOf("") }
        var medidoresClienteBusqueda by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        var selectedMedidorBusqueda by remember { mutableStateOf<Medidor?>(null) }
        var expandedMedidorDropdownBusqueda by remember { mutableStateOf(false) }


        var ultimaLectura by remember { mutableStateOf<LecturaConsumo?>(null) }
        var lecturasDelMes by remember { mutableStateOf<List<LecturaConsumo>>(emptyList()) }
        var error by remember { mutableStateOf<String?>(null) }

        // Estado para el diálogo de confirmación de eliminación
        var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
        var lecturaToDelete by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(rutClienteBusqueda) {
            if (rutClienteBusqueda.isNotBlank()) {
                try {
                    val cliente = clienteRepo.obtenerPorRut(rutClienteBusqueda)
                    if (cliente != null) {
                        medidoresClienteBusqueda = medidorRepo.listarPorCliente(rutClienteBusqueda)
                        selectedMedidorBusqueda = null // No pre-seleccionar
                        error = null
                    } else {
                        medidoresClienteBusqueda = emptyList()
                        selectedMedidorBusqueda = null
                        error = "Cliente no encontrado."
                    }
                } catch (e: Exception) {
                    medidoresClienteBusqueda = emptyList()
                    selectedMedidorBusqueda = null
                    error = e.message ?: "Error al buscar cliente o medidores."
                }
            } else {
                medidoresClienteBusqueda = emptyList()
                selectedMedidorBusqueda = null
                error = null
            }
        }

        // Función para realizar la búsqueda y actualizar los estados
        val performSearch: () -> Unit = { ->
            val anioInt = anio.toIntOrNull()
            val mesInt = mes?.toIntOrNull()
            val medidorIdToSearch = selectedMedidorBusqueda?.getCodigo()

            if (medidorIdToSearch != null && anioInt != null && mesInt != null) {
                lecturasDelMes = repo.listarPorMedidorMes(medidorIdToSearch, anioInt, mesInt)
                ultimaLectura = repo.ultimaLectura(medidorIdToSearch)

                if (lecturasDelMes.isEmpty() && ultimaLectura == null) {
                    error = "No existe un medidor con ese ID o no hay lecturas para el período."
                } else {
                    error = null
                }

            } else {
                error = "Complete todos los campos correctamente para buscar."
            }
        }

        // Callback para eliminar una lectura
        val onDeleteLectura: (String) -> Unit = { idLectura ->
            lecturaToDelete = idLectura
            showDeleteConfirmationDialog = true
        }

        Scaffold(
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderSection()

                // Formulario de Búsqueda
                SearchCard(
                    rutCliente = rutClienteBusqueda,
                    onRutClienteChange = { rutClienteBusqueda = it },
                    medidoresCliente = medidoresClienteBusqueda,
                    selectedMedidor = selectedMedidorBusqueda,
                    onSelectedMedidorChange = { selectedMedidorBusqueda = it },
                    expandedMedidorDropdown = expandedMedidorDropdownBusqueda,
                    onExpandedMedidorDropdownChange = { expandedMedidorDropdownBusqueda = it },
                    anio = anio, onAnioChange = { anio = it },
                    mes = mes, onMesChange = { mes = it },
                    onSearch = performSearch,
                    searchError = error
                )

                // Mostrar Última Lectura
                ultimaLectura?.let {
                    LastReadingCard(it)
                }

                // Tabla de Resultados
                if (lecturasDelMes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ResultsTable(lecturasDelMes, onDeleteLectura)
                }
            }

            // Diálogo de confirmación de eliminación
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Confirmar Eliminación") },
                    text = { Text("¿Estás seguro de que quieres eliminar esta lectura?") },
                    confirmButton = {
                        TextButton(onClick = {
                            lecturaToDelete?.let { id ->
                                repo.eliminarLectura(id)
                                performSearch() // Volver a cargar las lecturas después de eliminar
                            }
                            showDeleteConfirmationDialog = false
                            lecturaToDelete = null
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirmationDialog = false
                            lecturaToDelete = null
                        }) { Text("Cancelar") }
                    }
                )
            }
        }
    }

    /** Esta función gestiona su propio estado interno para los campos del formulario y
    * contiene la lógica para cargar datos (medidores) y validar la entrada del usuario.
     **/
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
        val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        var selectedMes by remember { mutableStateOf<String?>(null) }
        var expandedMesDropdown by remember { mutableStateOf(false) }
        var consumo by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        // Efecto para cargar medidores cuando el RUT del cliente cambia
        LaunchedEffect(rutCliente) {
            if (rutCliente.isNotBlank()) {
                try {
                    val cliente = clienteRepo.obtenerPorRut(rutCliente)
                    if (cliente != null) {
                        medidoresCliente = medidorRepo.listarPorCliente(rutCliente)
                        selectedMedidor = null // No pre-seleccionar
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
                        placeholder = { Text("Ejemplo: 12345678-9") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Medidor
                    ExposedDropdownMenuBox(
                        expanded = expandedMedidorDropdown,
                        onExpandedChange = { expandedMedidorDropdown = !expandedMedidorDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMedidor?.let { "Código: ${it.getCodigo()} - Dirección: ${it.getDireccionSuministro()}" } ?: "Seleccione un medidor",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Medidor") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedidorDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = expandedMedidorDropdown,
                            onDismissRequest = { expandedMedidorDropdown = false }
                        ) {
                            medidoresCliente.forEach { medidor ->
                                DropdownMenuItem(text = { Text("Código: ${medidor.getCodigo()} - Dirección: ${medidor.getDireccionSuministro()}") }, onClick = {
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
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        // Selector de Mes
                        ExposedDropdownMenuBox(
                            expanded = expandedMesDropdown,
                            onExpandedChange = { expandedMesDropdown = !expandedMesDropdown },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedMes ?: "Seleccione un mes",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mes") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMesDropdown) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expandedMesDropdown,
                                onDismissRequest = { expandedMesDropdown = false }
                            ) {
                                meses.forEach { mes ->
                                    DropdownMenuItem(text = { Text(mes) }, onClick = {
                                        selectedMes = mes
                                        expandedMesDropdown = false
                                    })
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = consumo,
                        onValueChange = { consumo = it },
                        label = { Text("Consumo (kWh)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                                val mesInt = selectedMes?.let { meses.indexOf(it) + 1 }
                                val consumoDouble = consumo.toDoubleOrNull()

                                if (rutCliente.isBlank() || selectedMedidor == null || anioInt == null || mesInt == null || consumoDouble == null) {
                                    error = "Todos los campos son obligatorios y deben ser válidos."
                                    return@Button
                                }

                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.YEAR, anioInt)
                                calendar.set(Calendar.MONTH, mesInt - 1)
                                calendar.set(Calendar.DAY_OF_MONTH, 1)
                                calendar.set(Calendar.HOUR_OF_DAY, 0)
                                calendar.set(Calendar.MINUTE, 0)
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
                                val lecturaDate = calendar.time

                                val newId = "${rutCliente}-${selectedMedidor!!.getCodigo()}-${anioInt}-${mesInt}"

                                onSave(
                                    LecturaConsumo(
                                        id = newId,
                                        createdAt = lecturaDate,
                                        updatedAt = lecturaDate,
                                        idMedidor = selectedMedidor!!.getCodigo(),
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

    /** Composable que representa la tarjeta de búsqueda de lecturas. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SearchCard(
        rutCliente: String, onRutClienteChange: (String) -> Unit,
        medidoresCliente: List<Medidor>,
        selectedMedidor: Medidor?, onSelectedMedidorChange: (Medidor?) -> Unit,
        expandedMedidorDropdown: Boolean, onExpandedMedidorDropdownChange: (Boolean) -> Unit,
        anio: String, onAnioChange: (String) -> Unit,
        mes: String?, onMesChange: (String?) -> Unit,
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
                    value = rutCliente,
                    onValueChange = onRutClienteChange,
                    label = { Text("RUT Cliente") },
                    placeholder = { Text("Ejemplo: 12345678-9") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedMedidorDropdown,
                    onExpandedChange = onExpandedMedidorDropdownChange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMedidor?.let { "Código: ${it.getCodigo()} - Dirección: ${it.getDireccionSuministro()}" } ?: "Seleccione un medidor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Medidor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedidorDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expandedMedidorDropdown,
                        onDismissRequest = { onExpandedMedidorDropdownChange(false) }
                    ) {
                        medidoresCliente.forEach { medidor ->
                            DropdownMenuItem(text = { Text("Código: ${medidor.getCodigo()} - Dirección: ${medidor.getDireccionSuministro()}") }, onClick = {
                                onSelectedMedidorChange(medidor)
                                onExpandedMedidorDropdownChange(false)
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = anio,
                        onValueChange = onAnioChange,
                        label = { Text("Año") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    val meses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                    var expandedMesDropdown by remember { mutableStateOf(false) }
                    val selectedMesName = remember(mes) { mes?.let { meses.getOrNull(it.toInt() - 1) } }

                    ExposedDropdownMenuBox(
                        expanded = expandedMesDropdown,
                        onExpandedChange = { expandedMesDropdown = !expandedMesDropdown },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedMesName ?: "Seleccione un mes",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mes") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMesDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMesDropdown,
                            onDismissRequest = { expandedMesDropdown = false }
                        ) {
                            meses.forEachIndexed { index, mesName ->
                                DropdownMenuItem(text = { Text(mesName) }, onClick = {
                                    onMesChange((index + 1).toString())
                                    expandedMesDropdown = false
                                })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                searchError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = onSearch,
                    enabled = rutCliente.isNotBlank() && selectedMedidor != null && anio.isNotBlank() && mes != null,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Buscar")
                }
            }
        }
    }

    /** Formatea una fecha para mostrarla en un formato legible. */
    private fun formatDisplayDate(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(date)
    }

    @Composable
    private fun LastReadingCard(lectura: LecturaConsumo) {
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
    /** Muestra los resultados de los consumos */
    private fun ResultsTable(lecturas: List<LecturaConsumo>, onDelete: (String) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 24.dp)
        ) {
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
                Text(
                    "Acciones",
                    modifier = Modifier.weight(0.8f),
                    fontWeight = FontWeight.Bold,
                    color = blue,
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider()
            // Filas de la tabla
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(lecturas.sortedByDescending { it.getCreatedAt() }) { lectura ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatDisplayDate(lectura.getCreatedAt()), modifier = Modifier.weight(1f))
                        Text(
                            lectura.getKwhLeidos().toString(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                        Box(
                            modifier = Modifier.weight(0.8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { onDelete(lectura.getId()) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Eliminar", fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
