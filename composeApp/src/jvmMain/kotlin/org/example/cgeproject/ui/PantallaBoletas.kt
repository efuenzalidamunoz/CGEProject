package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.persistencia.*
import org.example.cgeproject.servicios.BoletaService
import org.example.cgeproject.servicios.PdfService
import org.example.cgeproject.servicios.TarifaService
import java.io.File
import javax.swing.JFileChooser
import java.text.SimpleDateFormat
import java.util.Date

// Enum para controlar la navegación entre pantallas
private enum class PantallaBoleta {
    LISTA,
    FORMULARIO
}

class PantallaBoletas {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    // Se mantienen tus inicializaciones de servicios
    private val boletaService: BoletaService
    private val repo: BoletaRepoImpl
    private val medidorRepo: MedidorRepoImpl
    private val lecturaRepo: LecturaRepoImpl // Agregamos el repositorio de lecturas

    init {
        val persistencia = PersistenciaDatos(FileSystemStorageDriver())
        val clienteRepo = ClienteRepoImpl(persistencia)
        medidorRepo = MedidorRepoImpl(persistencia)
        lecturaRepo = LecturaRepoImpl(persistencia) // Inicializamos lecturaRepo
        repo = BoletaRepoImpl(persistencia)
        val tarifaService = TarifaService()
        val pdfService = PdfService()
        boletaService = BoletaService(clienteRepo, medidorRepo, lecturaRepo, repo, tarifaService, pdfService)
    }

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaBoleta.LISTA) }

        when (pantallaActual) {
            PantallaBoleta.LISTA -> {
                ListarBoletasContent(
                    onNavigateToForm = { pantallaActual = PantallaBoleta.FORMULARIO }
                )
            }

            PantallaBoleta.FORMULARIO -> {
                FormularioBoletaContent(
                    onNavigateBack = { pantallaActual = PantallaBoleta.LISTA },
                    onSaveBoleta = { rut, codigoMedidor, anio, mes, kwhConsumido -> // Actualizamos la firma
                        boletaService.emitirBoletaMensual(rut, codigoMedidor, anio, mes, kwhConsumido)
                        pantallaActual = PantallaBoleta.LISTA
                    },
                    medidorRepo = medidorRepo,
                    lecturaRepo = lecturaRepo // Pasamos el lecturaRepo al composable
                )
            }
        }
    }

    @Composable
    private fun ListarBoletasContent(onNavigateToForm: () -> Unit) {
        var idCliente by remember { mutableStateOf("") }
        var boletas by remember { mutableStateOf<List<Boleta>>(emptyList()) }
        var boletaParaDetalle by remember { mutableStateOf<Boleta?>(null) }
        var busquedaRealizada by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(32.dp))
                BodyBusqueda(
                    idCliente = idCliente,
                    onIdClienteChange = { idCliente = it },
                    onSearch = {
                        boletas = repo.listarPorCliente(idCliente)
                        busquedaRealizada = true
                    }
                )

                if (busquedaRealizada) {
                    Spacer(modifier = Modifier.height(24.dp))
                    MostrarTablaBoletas(idCliente, boletas, onVerDetalle = {
                        boletaParaDetalle = it
                    })
                }
            }
        }

        boletaParaDetalle?.let {
            DetalleBoletaDialog(
                boleta = it,
                onDismiss = { boletaParaDetalle = null },
                onGeneratePdf = {
                    val pdfBytes = boletaService.exportarPdfClienteMes(it.getIdCliente(), it.getMes(), it.getAnio())
                    val fileChooser = JFileChooser()
                    fileChooser.selectedFile = File("boleta_${it.getIdCliente()}_${it.getAnio()}_${it.getMes()}.pdf")
                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        fileChooser.selectedFile.writeBytes(pdfBytes)
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FormularioBoletaContent(
        onNavigateBack: () -> Unit,
        onSaveBoleta: (String, String, Int, Int, Double) -> Unit, // Actualizamos la firma
        medidorRepo: MedidorRepoImpl,
        lecturaRepo: LecturaRepoImpl // Recibimos el lecturaRepo
    ) {
        var rut by remember { mutableStateOf("") }
        var anio by remember { mutableStateOf("") }
        var mes by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        var medidoresCliente by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        var selectedMedidor by remember { mutableStateOf<Medidor?>(null) }
        var expandedMedidorDropdown by remember { mutableStateOf(false) }

        var lecturasDisponibles by remember { mutableStateOf<List<LecturaConsumo>>(emptyList()) }
        var selectedLectura by remember { mutableStateOf<LecturaConsumo?>(null) }
        var expandedLecturaDropdown by remember { mutableStateOf(false) }

        // Efecto para cargar medidores cuando el RUT cambia
        LaunchedEffect(rut) {
            if (rut.isNotBlank()) {
                medidoresCliente = medidorRepo.listarPorCliente(rut)
                selectedMedidor = medidoresCliente.firstOrNull()
                if (medidoresCliente.isEmpty()) {
                    error = "No se encontraron medidores para el RUT proporcionado."
                } else {
                    error = null
                }
            } else {
                medidoresCliente = emptyList()
                selectedMedidor = null
                error = null
            }
            // Resetear lecturas cuando el medidor o RUT cambian
            lecturasDisponibles = emptyList()
            selectedLectura = null
        }

        // Efecto para cargar lecturas cuando el medidor, año o mes cambian
        LaunchedEffect(selectedMedidor, anio, mes) {
            val anioInt = anio.toIntOrNull()
            val mesInt = mes.toIntOrNull()
            if (selectedMedidor != null && anioInt != null && mesInt != null) {
                lecturasDisponibles = lecturaRepo.listarPorMedidorMes(selectedMedidor!!.getCodigo(), anioInt, mesInt)
                selectedLectura = lecturasDisponibles.firstOrNull()
                if (lecturasDisponibles.isEmpty()) {
                    error = "No se encontraron lecturas para el medidor y período seleccionados."
                } else {
                    error = null
                }
            } else {
                lecturasDisponibles = emptyList()
                selectedLectura = null
                // No limpiar el error si ya existe uno por falta de medidores o RUT
                if (error == "No se encontraron lecturas para el medidor y período seleccionados.") {
                    error = null
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Emitir Nueva Boleta", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = { rut = it },
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

                    OutlinedTextField(
                        value = anio,
                        onValueChange = { anio = it },
                        label = { Text("Año (YYYY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mes,
                        onValueChange = { mes = it },
                        label = { Text("Mes (1-12)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selector de Lectura
                    ExposedDropdownMenuBox(
                        expanded = expandedLecturaDropdown,
                        onExpandedChange = { expandedLecturaDropdown = !expandedLecturaDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedLectura?.let { formatLecturaForDisplay(it) } ?: "Seleccione una lectura",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lectura") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLecturaDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedLecturaDropdown,
                            onDismissRequest = { expandedLecturaDropdown = false }
                        ) {
                            lecturasDisponibles.forEach { lectura ->
                                DropdownMenuItem(text = { Text(formatLecturaForDisplay(lectura)) }, onClick = {
                                    selectedLectura = lectura
                                    expandedLecturaDropdown = false
                                })
                            }
                        }
                    }

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

                                if (rut.isBlank()) {
                                    error = "El RUT del cliente es obligatorio."
                                    return@Button
                                }
                                if (selectedMedidor == null) {
                                    error = "Debe seleccionar un medidor."
                                    return@Button
                                }
                                if (anioInt == null) {
                                    error = "El año es obligatorio y debe ser un número válido."
                                    return@Button
                                }
                                if (mesInt == null) {
                                    error = "El mes es obligatorio y debe ser un número válido."
                                    return@Button
                                }
                                if (selectedLectura == null) {
                                    error = "Debe seleccionar una lectura."
                                    return@Button
                                }

                                try {
                                    onSaveBoleta(rut, selectedMedidor!!.getCodigo(), anioInt, mesInt, selectedLectura!!.getKwhLeidos())
                                } catch (e: Exception) {
                                    error = e.message ?: "Error desconocido al emitir la boleta."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = blue)
                        ) { Text("Emitir y Guardar") }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = onNavigateBack) { Text("Cancelar") }
                    }
                }
            }
        }
    }

    // --- Composables del diseño original (Reutilizados y adaptados) ---

    @Composable
    private fun HeaderSection() {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(blue)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().padding(100.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Detalle de boleta", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Si tienes una consulta con el detalle de la boleta, aquí lo podrás encontrar.",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun BodyBusqueda(idCliente: String, onIdClienteChange: (String) -> Unit, onSearch: () -> Unit) {
        ElevatedCard(
            modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Consultar Historial de Boletas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = blue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = idCliente,
                    onValueChange = onIdClienteChange,
                    label = { Text("Ingresar el número de cliente") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSearch,
                    enabled = idCliente.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Buscar")
                }
            }
        }
    }

    @Composable
    private fun MostrarTablaBoletas(idCliente: String, boletas: List<Boleta>, onVerDetalle: (Boleta) -> Unit) {
        ElevatedCard(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth(0.7f)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Resultados para Cliente: $idCliente",
                    style = MaterialTheme.typography.titleLarge,
                    color = blue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().background(blue.copy(alpha = 0.1f)).padding(12.dp)) {
                    HeaderTabla("Fecha", Modifier.weight(1f))
                    HeaderTabla("Consumo (kWh)", Modifier.weight(1f))
                    HeaderTabla("Total a Pagar", Modifier.weight(1f))
                    HeaderTabla("Acciones", Modifier.weight(1f))
                }
                HorizontalDivider()
                if (boletas.isEmpty()) {
                    Text(
                        "No se encontraron boletas.",
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    boletas.forEach { boleta ->
                        FilasTabla(boleta, onVerDetalle)
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderTabla(text: String, modifier: Modifier = Modifier) {
        Text(text, fontWeight = FontWeight.Bold, color = blue, modifier = modifier)
    }

    @Composable
    private fun FilasTabla(boleta: Boleta, onVerDetalle: (Boleta) -> Unit) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${boleta.getMes()}/${boleta.getAnio()}", modifier = Modifier.weight(1f))
            Text("%.2f".format(boleta.getKwhTotal()), modifier = Modifier.weight(1f))
            Text(
                "$${boleta.getDetalle().total.toInt()}",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { onVerDetalle(boleta) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text("Ver Detalle")
            }
        }
    }

    @Composable
    private fun DetalleBoletaDialog(boleta: Boleta, onDismiss: () -> Unit, onGeneratePdf: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Detalle de Boleta (${boleta.getMes()}/${boleta.getAnio()})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val detalle = boleta.getDetalle()
                    Text("Cliente: ${boleta.getIdCliente()}", fontWeight = FontWeight.Bold)
                    Text("Consumo Total: ${"%.2f".format(boleta.getKwhTotal())} kWh")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Subtotal: $${detalle.subtotal.toInt()}")
                    Text("Cargos Adicionales: $${detalle.cargos.toInt()}")
                    Text("IVA (19%): $${detalle.iva.toInt()}")
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Monto Total: $${detalle.total.toInt()}",
                        fontWeight = FontWeight.Bold,
                        color = blue,
                        fontSize = 18.sp
                    )
                }
            },
            confirmButton = {
                Row {
                    Button(onClick = onGeneratePdf, colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                        Text("Generar PDF")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        )
    }

    private fun formatLecturaForDisplay(lectura: LecturaConsumo): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return "Fecha: ${formatter.format(lectura.getCreatedAt())}, Consumo: ${"%.2f".format(lectura.getKwhLeidos())} kWh"
    }
}
