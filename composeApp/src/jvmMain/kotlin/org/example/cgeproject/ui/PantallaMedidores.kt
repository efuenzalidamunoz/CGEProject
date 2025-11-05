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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico
import org.example.cgeproject.persistencia.ClienteRepositorio
import org.example.cgeproject.persistencia.MedidorRepositorio

private enum class PantallaMedidor {
    LISTA,
    FORMULARIO
}

class PantallaMedidores(private val medidorRepositorio: MedidorRepositorio, private val clienteRepositorio: ClienteRepositorio) {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaMedidor.LISTA) }
        val scope = rememberCoroutineScope()

        var listaDeClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }

        LaunchedEffect(Unit) {
            listaDeClientes = clienteRepositorio.listar()
        }

        when (pantallaActual) {
            PantallaMedidor.LISTA -> {
                GestionMedidoresContent(
                    onNavigateToForm = { pantallaActual = PantallaMedidor.FORMULARIO }
                )
            }
            PantallaMedidor.FORMULARIO -> {
                FormularioMedidorContent(
                    clientes = listaDeClientes,
                    onNavigateBack = { pantallaActual = PantallaMedidor.LISTA },
                    onSave = { medidor, rutCliente ->
                        scope.launch {
                            medidorRepositorio.crear(medidor, rutCliente)
                            pantallaActual = PantallaMedidor.LISTA
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionMedidoresContent(onNavigateToForm: () -> Unit) {
        var searchQuery by remember { mutableStateOf("") }
        var medidoresEncontrados by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        var medidorParaDetalle by remember { mutableStateOf<Medidor?>(null) }
        var medidorParaEliminar by remember { mutableStateOf<Medidor?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(searchQuery) {
            if (searchQuery.isBlank()) {
                medidoresEncontrados = emptyList()
            }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Gestión de Medidores", color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = blue)) },
            floatingActionButton = { FloatingActionButton(onClick = onNavigateToForm, containerColor = blue) { Text("+", color = Color.White, fontSize = 24.sp) } }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(paddingValues).padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por RUT de Cliente o Código de Medidor") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Button(onClick = {
                            scope.launch {
                                val medidorPorCodigo = medidorRepositorio.obtenerPorCodigo(searchQuery)
                                if (medidorPorCodigo != null) {
                                    medidoresEncontrados = listOf(medidorPorCodigo)
                                } else {
                                    medidoresEncontrados = medidorRepositorio.listarPorCliente(searchQuery)
                                }
                            }
                        }) { Text("Buscar") }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (medidoresEncontrados.isEmpty()) {
                    Text("Ingrese un RUT o código para buscar medidores.", modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(medidoresEncontrados) { medidor ->
                            MedidorItem(
                                medidor = medidor,
                                onClick = { medidorParaDetalle = medidor },
                                onDelete = { medidorParaEliminar = medidor }
                            )
                        }
                    }
                }
            }

            medidorParaDetalle?.let { DetalleMedidorDialog(medidor = it, onDismiss = { medidorParaDetalle = null }) }

            medidorParaEliminar?.let {
                EliminarMedidorDialog(
                    medidor = it,
                    onDismiss = { medidorParaEliminar = null },
                    onConfirm = {
                        scope.launch {
                            medidorRepositorio.eliminar(it.getCodigo())
                            val medidorPorCodigo = medidorRepositorio.obtenerPorCodigo(searchQuery)
                            if (medidorPorCodigo != null) {
                                medidoresEncontrados = listOf(medidorPorCodigo)
                            } else {
                                medidoresEncontrados = medidorRepositorio.listarPorCliente(searchQuery)
                            }
                            medidorParaEliminar = null
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun FormularioMedidorContent(clientes: List<Cliente>, onNavigateBack: () -> Unit, onSave: (Medidor, String) -> Unit) {
        var codigo by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf("Monofásico") }
        var direccion by remember { mutableStateOf("") }
        var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        Column(modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 50.dp)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Registrar Nuevo Medidor", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código del Medidor") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección de Suministro") }, modifier = Modifier.fillMaxWidth())
                    
                    // Selector de Tipo de Medidor
                    val tipos = listOf("Monofásico", "Trifásico")
                    TipoDropDown(tipos = tipos, selectedTipo = tipo, onTipoSelected = { tipo = it })

                    ClienteDropDown(clientes = clientes, selectedCliente = clienteSeleccionado, onClienteSelected = { clienteSeleccionado = it })

                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = {
                                val rut = clienteSeleccionado?.getRut()
                                if (codigo.isBlank() || direccion.isBlank() || rut == null) {
                                    error = "Todos los campos son obligatorios."
                                    return@Button
                                }
                                val nuevoMedidor: Medidor = when (tipo) {
                                    "Monofásico" -> MedidorMonofasico(
                                        id = "med-$codigo", createdAt = Clock.System.now(), updatedAt = Clock.System.now(),
                                        codigo = codigo, direccionSuministro = direccion, activo = true, idCliente = rut,
                                        potenciaMaxKw = 5.5 // Valor de ejemplo
                                    )
                                    "Trifásico" -> MedidorTrifasico(
                                        id = "med-$codigo", createdAt = Clock.System.now(), updatedAt = Clock.System.now(),
                                        codigo = codigo, direccionSuministro = direccion, activo = true, idCliente = rut,
                                        potenciaMaxKw = 10.0, factorPotencia = 0.95 // Valores de ejemplo
                                    )
                                    else -> Medidor(
                                        id = "med-$codigo", createdAt = Clock.System.now(), updatedAt = Clock.System.now(),
                                        codigo = codigo, direccionSuministro = direccion, activo = true, idCliente = rut
                                    )
                                }
                                onSave(nuevoMedidor, rut)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = blue)
                        ) { Text("Guardar Medidor") }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = onNavigateBack) { Text("Cancelar") }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TipoDropDown(tipos: List<String>, selectedTipo: String, onTipoSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedTipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Medidor") },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.5f)) {
                tipos.forEach { tipo ->
                    DropdownMenuItem(text = { Text(tipo) }, onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    })
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ClienteDropDown(clientes: List<Cliente>, selectedCliente: Cliente?, onClienteSelected: (Cliente) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selectedCliente?.getNombre() ?: "Seleccione un cliente",
                onValueChange = {},
                readOnly = true,
                label = { Text("Cliente Asociado") },
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.5f)) {
                clientes.forEach { cliente ->
                    DropdownMenuItem(text = { Text("${cliente.getNombre()} (${cliente.getRut()})") }, onClick = {
                        onClienteSelected(cliente)
                        expanded = false
                    })
                }
            }
        }
    }

    @Composable
    private fun MedidorItem(medidor: Medidor, onClick: () -> Unit, onDelete: () -> Unit) {
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(medidor.getCodigo(), fontWeight = FontWeight.Bold, color = blue)
                    Text("Tipo: ${medidor.tipo()}", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") }
            }
        }
    }

    @Composable
    private fun DetalleMedidorDialog(medidor: Medidor, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Detalle del Medidor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Código: ${medidor.getCodigo()}", fontWeight = FontWeight.Bold)
                    Text("Tipo: ${medidor.tipo()}")
                    Text("Asociado al cliente: ${medidor.getIdCliente()}")
                }
            },
            confirmButton = { Button(onClick = onDismiss) { Text("Cerrar") } }
        )
    }

    @Composable
    private fun EliminarMedidorDialog(medidor: Medidor, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Seguro que deseas eliminar el medidor con código '${medidor.getCodigo()}'?") },
            confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") } },
            dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
        )
    }
}