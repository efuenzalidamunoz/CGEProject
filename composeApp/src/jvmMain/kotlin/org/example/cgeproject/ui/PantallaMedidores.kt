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

// --- Modelo de Dominio (Asumido) ---
// TODO: Reemplazar con tu clase de dominio real.
data class Medidor(
    val codigo: String,
    val tipo: String, // E.g., "Monofásico", "Trifásico"
    val rutCliente: String
)

// --- Enum para Navegación ---
private enum class PantallaMedidor {
    LISTA,
    FORMULARIO
}

class PantallaMedidores {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    // --- Datos de Ejemplo ---
    // TODO: Reemplazar con las llamadas a tu repositorio.
    private val medidoresDeEjemplo = listOf(
        Medidor("MED-001", "Monofásico", "111-1"),
        Medidor("MED-002", "Monofásico", "111-1"),
        Medidor("MED-003", "Trifásico", "222-2")
    )

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaMedidor.LISTA) }

        when (pantallaActual) {
            PantallaMedidor.LISTA -> {
                GestionMedidoresContent(
                    onNavigateToForm = { pantallaActual = PantallaMedidor.FORMULARIO }
                )
            }

            PantallaMedidor.FORMULARIO -> {
                FormularioMedidorContent(
                    onNavigateBack = { pantallaActual = PantallaMedidor.LISTA },
                    onSave = { medidor ->
                        // TODO: Implementar lógica de 'crear(m: Medidor, rutCliente: String)'
                        println("Guardando nuevo medidor: $medidor")
                        pantallaActual = PantallaMedidor.LISTA
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionMedidoresContent(onNavigateToForm: () -> Unit) {
        var searchQuery by remember { mutableStateOf("") }
        var medidoresFiltrados by remember { mutableStateOf<List<Medidor>>(emptyList()) }
        var medidorParaDetalle by remember { mutableStateOf<Medidor?>(null) }
        var medidorParaEliminar by remember { mutableStateOf<Medidor?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de Medidores", color = Color.White) },
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
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        // TODO: Implementar lógica de 'listarPorCliente' y 'obtenerPorCodigo'
                        medidoresFiltrados = if (searchQuery.isNotBlank()) {
                            medidoresDeEjemplo.filter { medidor ->
                                medidor.rutCliente.contains(searchQuery, true) || medidor.codigo.contains(
                                    searchQuery,
                                    true
                                )
                            }
                        } else {
                            emptyList()
                        }
                    },
                    label = { Text("Buscar por RUT de Cliente o Código de Medidor") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de resultados
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(medidoresFiltrados) { medidor ->
                        MedidorItem(
                            medidor = medidor,
                            onClick = { medidorParaDetalle = medidor },
                            onDelete = { medidorParaEliminar = medidor }
                        )
                    }
                }
            }

            // Diálogo de detalles
            medidorParaDetalle?.let {
                DetalleMedidorDialog(medidor = it, onDismiss = { medidorParaDetalle = null })
            }

            // Diálogo de eliminación
            medidorParaEliminar?.let {
                EliminarMedidorDialog(
                    medidor = it,
                    onDismiss = { medidorParaEliminar = null },
                    onConfirm = {
                        // TODO: Implementar lógica de 'eliminar(codigo)'
                        println("Eliminando medidor: ${it.codigo}")
                        medidorParaEliminar = null
                    }
                )
            }
        }
    }

    @Composable
    private fun FormularioMedidorContent(onNavigateBack: () -> Unit, onSave: (Medidor) -> Unit) {
        var codigo by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf("") }
        var rutCliente by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 50.dp)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text("Registrar Nuevo Medidor", style = MaterialTheme.typography.headlineMedium, color = blue)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código del Medidor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = { tipo = it },
                        label = { Text("Tipo de Medidor (Ej: Monofásico)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rutCliente,
                        onValueChange = { rutCliente = it },
                        label = { Text("RUT del Cliente Asociado") },
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
                                if (codigo.isBlank() || tipo.isBlank() || rutCliente.isBlank()) {
                                    error = "Todos los campos son obligatorios."
                                    return@Button
                                }
                                onSave(Medidor(codigo, tipo, rutCliente))
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

    @Composable
    private fun MedidorItem(medidor: Medidor, onClick: () -> Unit, onDelete: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(medidor.codigo, fontWeight = FontWeight.Bold, color = blue)
                    Text("Tipo: ${medidor.tipo}", style = MaterialTheme.typography.bodyMedium)
                    Text("Cliente: ${medidor.rutCliente}", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
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
                    Text("Código: ${medidor.codigo}", fontWeight = FontWeight.Bold)
                    Text("Tipo: ${medidor.tipo}")
                    Text("Asociado al cliente: ${medidor.rutCliente}")
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("Cerrar") }
            }
        )
    }

    @Composable
    private fun EliminarMedidorDialog(medidor: Medidor, onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Seguro que deseas eliminar el medidor con código '${medidor.codigo}'?") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }
}