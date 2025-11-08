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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.persistencia.*
import org.example.cgeproject.servicios.BoletaService
import org.example.cgeproject.servicios.EmailService
import org.example.cgeproject.servicios.PdfService
import org.example.cgeproject.servicios.TarifaService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane

// Enum para controlar la navegación entre pantallas
private enum class PantallaBoleta {
    LISTA,
    FORMULARIO
}

class PantallaBoletas {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    private val boletaService: BoletaService
    private val emailService: EmailService
    private val repo: BoletaRepoImpl
    private val medidorRepo: MedidorRepoImpl
    private val lecturaRepo: LecturaRepoImpl
    private val clienteRepo: ClienteRepoImpl

    init {
        val persistencia = PersistenciaDatos(FileSystemStorageDriver())
        clienteRepo = ClienteRepoImpl(persistencia)
        medidorRepo = MedidorRepoImpl(persistencia)
        lecturaRepo = LecturaRepoImpl(persistencia)
        repo = BoletaRepoImpl(persistencia)
        val tarifaService = TarifaService()
        val pdfService = PdfService()
        emailService = EmailService()
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
                    onSaveBoleta = { rut, codigoMedidor, anio, mes, kwhConsumido ->
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
    /** Enlista en una tabla todo el contenido de las boletas */
    private fun ListarBoletasContent(onNavigateToForm: () -> Unit) {
        var idCliente by remember { mutableStateOf("") }
        var boletas by remember { mutableStateOf<List<Boleta>>(emptyList()) }
        var boletaParaDetalle by remember { mutableStateOf<Boleta?>(null) }
        var boletaParaEliminar by remember { mutableStateOf<Boleta?>(null) }
        var busquedaRealizada by remember { mutableStateOf(false) }
        var errorBusqueda by remember { mutableStateOf<String?>(null) }
        var isSendingEmail by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

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
                    onIdClienteChange = {
                        idCliente = it
                        busquedaRealizada = false
                        errorBusqueda = null
                    },
                    onSearch = {
                        busquedaRealizada = true
                        if (clienteRepo.obtenerPorRut(idCliente) == null) {
                            errorBusqueda = "No se encontró un cliente con el RUT proporcionado."
                            boletas = emptyList()
                        } else {
                            errorBusqueda = null
                            boletas = repo.listarPorCliente(idCliente)
                        }
                    }
                )

                if (busquedaRealizada) {
                    Spacer(modifier = Modifier.height(24.dp))
                    if (errorBusqueda != null) {
                        ElevatedCard(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                        ) {
                            Text(
                                text = errorBusqueda!!,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                            )
                        }
                    } else {
                        MostrarTablaBoletas(
                            idCliente,
                            boletas,
                            onVerDetalle = { boletaParaDetalle = it },
                            onEliminarBoleta = { boletaParaEliminar = it }
                        )
                    }
                }
            }
        }

        LoadingOverlay(isLoading = isSendingEmail)

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
                },
                onSendEmail = {
                    coroutineScope.launch {
                        isSendingEmail = true
                        try {
                            val pdfBytes = withContext(Dispatchers.IO) {
                                boletaService.exportarPdfClienteMes(it.getIdCliente(), it.getMes(), it.getAnio())
                            }
                            val cliente = clienteRepo.obtenerPorRut(it.getIdCliente())
                            if (cliente != null && cliente.getEmail().isNotBlank()) {
                                withContext(Dispatchers.IO) {
                                    emailService.enviarBoletaPorCorreo(
                                        cliente.getEmail(),
                                        "Boleta CGE ${it.getMes()}/${it.getAnio()}",
                                        "Estimado(a) ${cliente.getNombre()}, Adjuntamos su boleta del mes ${it.getMes()} del año ${it.getAnio()}.",
                                        pdfBytes,
                                        "boleta_${it.getIdCliente()}_${it.getAnio()}_${it.getMes()}.pdf"
                                    )
                                }
                                JOptionPane.showMessageDialog(null, "Boleta enviada exitosamente a ${cliente.getEmail()}", "Envío Exitoso", JOptionPane.INFORMATION_MESSAGE)
                            } else {
                                JOptionPane.showMessageDialog(null, "El cliente no tiene un correo electrónico registrado o es inválido.", "Error", JOptionPane.ERROR_MESSAGE)
                            }
                        } catch (e: Exception) {
                            JOptionPane.showMessageDialog(null, "Error al enviar el correo: ${e.message}", "Error de Envío", JOptionPane.ERROR_MESSAGE)
                        } finally {
                            isSendingEmail = false
                        }
                    }
                }
            )
        }

        boletaParaEliminar?.let { boleta ->
            AlertDialog(
                onDismissRequest = { boletaParaEliminar = null },
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que deseas eliminar la boleta de ${boleta.getIdCliente()} para ${boleta.getMes()}/${boleta.getAnio()}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            boletaService.eliminarBoleta(boleta.getId())
                            boletas = repo.listarPorCliente(idCliente) // Refrescar la lista
                            boletaParaEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { boletaParaEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    /** Muestra una superposición de carga con un indicador de progreso circular. */
    @Composable
    private fun LoadingOverlay(isLoading: Boolean) {
        if (isLoading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = blue)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    /** Captura todos los datos necesarios para la boleta */
    private fun FormularioBoletaContent(
        onNavigateBack: () -> Unit,
        onSaveBoleta: (String, String, Int, Int, Double) -> Unit,
        medidorRepo: MedidorRepoImpl,
        lecturaRepo: LecturaRepoImpl
    ) {
        var rut by remember { mutableStateOf("") }
        var anio by remember { mutableStateOf("") }
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
                selectedMedidor = null // No pre-seleccionar
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

        // Efecto para cargar lecturas cuando el medidor o año cambian
        LaunchedEffect(selectedMedidor, anio) {
            val anioInt = anio.toIntOrNull()
            if (selectedMedidor != null && anioInt != null) {
                // Iterar por todos los meses para obtener las lecturas del año
                val lecturasDelAnio = mutableListOf<LecturaConsumo>()
                (1..12).forEach { mes ->
                    lecturasDelAnio.addAll(lecturaRepo.listarPorMedidorMes(selectedMedidor!!.getCodigo(), anioInt, mes))
                }
                lecturasDisponibles = lecturasDelAnio.sortedBy { it.getCreatedAt() }
                selectedLectura = null // No pre-seleccionar
                if (lecturasDisponibles.isEmpty()) {
                    error = "No se encontraron lecturas para el medidor y año seleccionados."
                } else {
                    error = null
                }
            } else {
                lecturasDisponibles = emptyList()
                selectedLectura = null
                // No limpiar el error si ya existe uno por falta de medidores o RUT
                if (error == "No se encontraron lecturas para el medidor y año seleccionados.") {
                    error = null
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ElevatedCard(
                modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Emitir Nueva Boleta", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = { rut = it },
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

                    OutlinedTextField(
                        value = anio,
                        onValueChange = { anio = it },
                        label = { Text("Año (YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
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
                                if (selectedLectura == null) {
                                    error = "Debe seleccionar una lectura."
                                    return@Button
                                }

                                val cal = Calendar.getInstance()
                                cal.time = selectedLectura!!.getCreatedAt()
                                val mesDeLectura = cal.get(Calendar.MONTH) + 1
                                val anioDeLectura = cal.get(Calendar.YEAR)

                                if (anioDeLectura != anioInt) {
                                    error = "El año de la lectura ($anioDeLectura) no coincide con el año ingresado ($anioInt)."
                                    return@Button
                                }

                                try {
                                    onSaveBoleta(
                                        rut,
                                        selectedMedidor!!.getCodigo(),
                                        anioDeLectura,
                                        mesDeLectura,
                                        selectedLectura!!.getKwhLeidos()
                                    )
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

    @Composable
    /**
     * Sección de encabezado para la pantalla de boletas.
     * Muestra un título y una descripción en un fondo azul.
     */
    private fun HeaderSection() {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(blue)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().padding(horizontal = 200.dp),
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

    /** Permite al usuario ingresar el rut del cliente y buscar las boletas asociadas */
    private fun BodyBusqueda(idCliente: String, onIdClienteChange: (String) -> Unit, onSearch: () -> Unit) {
        ElevatedCard(
            modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
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
                    label = { Text("RUT Cliente") },
                    placeholder = { Text("Ejemplo: 12345678-9") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
    /** Enlista las boletas */
    private fun MostrarTablaBoletas(
        idCliente: String,
        boletas: List<Boleta>,
        onVerDetalle: (Boleta) -> Unit,
        onEliminarBoleta: (Boleta) -> Unit
    ) {
        ElevatedCard(
            modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
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
                    HeaderTabla("Acciones", Modifier.weight(1.5f))
                }
                HorizontalDivider()
                if (boletas.isEmpty()) {
                    Text(
                        "No se encontraron boletas.",
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    boletas.forEach { boleta ->
                        FilasTabla(boleta, onVerDetalle, onEliminarBoleta) // Pasar la función a FilasTabla
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }

    @Composable
    /** Formato de la tabla boleta */
    private fun HeaderTabla(text: String, modifier: Modifier = Modifier) {
        Text(text, fontWeight = FontWeight.Bold, color = blue, modifier = modifier)
    }

    @Composable
    /** Formato de las filas de la tabla boleta */
    private fun FilasTabla(boleta: Boleta, onVerDetalle: (Boleta) -> Unit, onEliminarBoleta: (Boleta) -> Unit) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${boleta.getMes()}/${boleta.getAnio()}", modifier = Modifier.weight(1f))
            Text("%.2f".format(boleta.getKwhTotal()), modifier = Modifier.weight(1f))
            Text(
                "$${boleta.getDetalle().total.toInt()}",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Row(modifier = Modifier.weight(1.5f), horizontalArrangement = Arrangement.SpaceAround) {
                Button(
                    onClick = { onVerDetalle(boleta) },
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Ver Detalle")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onEliminarBoleta(boleta) }, // Botón de eliminar
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }

    /** Ventana de detalle de boleta */
    @Composable
    private fun DetalleBoletaDialog(boleta: Boleta, onDismiss: () -> Unit, onGeneratePdf: () -> Unit, onSendEmail: () -> Unit) {
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
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(onClick = onGeneratePdf, colors = ButtonDefaults.buttonColors(containerColor = blue)) {
                        Text("Generar PDF")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSendEmail, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))) {
                        Text("Enviar Email")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
            }
        )
    }

    /** Formato de la lectura */
    private fun formatLecturaForDisplay(lectura: LecturaConsumo): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return "Fecha: ${formatter.format(lectura.getCreatedAt())}, Consumo: ${"%.2f".format(lectura.getKwhLeidos())} kWh"
    }
}