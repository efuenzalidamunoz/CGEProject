package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.cgeproject.dominio.Medidor
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico
import org.example.cgeproject.persistencia.MedidorRepositorio
import java.util.Date
import java.util.UUID

private enum class PantallaMedidor {
    LISTA,
    FORMULARIO
}

private enum class TipoMedidor(val str: String) {
    MONOFASICO("Monofásico"),
    TRIFASICO("Trifásico")
}

class PantallaMedidores(private val repo: MedidorRepositorio) {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(PantallaMedidor.LISTA) }

        when (pantallaActual) {
            PantallaMedidor.LISTA -> {
                GestionMedidoresContent(
                    onNavigateToForm = { pantallaActual = PantallaMedidor.FORMULARIO },
                    onSearch = { query ->
                        if (query.isBlank()) return@GestionMedidoresContent emptyList()
                        // Asumimos que una búsqueda por código es más específica
                        val porCodigo = repo.obtenerPorCodigo(query)
                        if (porCodigo != null) {
                            listOf(porCodigo)
                        } else {
                            repo.listarPorCliente(query)
                        }
                    },
                    onDelete = { medidor -> repo.eliminar(medidor.getCodigo()) }
                )
            }

            PantallaMedidor.FORMULARIO -> {
                FormularioMedidorContent(
                    onNavigateBack = { pantallaActual = PantallaMedidor.LISTA },
                    onSave = { medidor, rut ->
                        repo.crear(medidor, rut)
                        pantallaActual = PantallaMedidor.LISTA
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionMedidoresContent(
        onNavigateToForm: () -> Unit,
        onSearch: (String) -> List<Medidor>,
        onDelete: (Medidor) -> Unit
    ) {
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        medidoresFiltrados = onSearch(it)
                    },
                    label = { Text("Buscar por RUT de Cliente o Código de Medidor") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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

            medidorParaDetalle?.let {
                DetalleMedidorDialog(medidor = it, onDismiss = { medidorParaDetalle = null })
            }

            medidorParaEliminar?.let {
                EliminarMedidorDialog(
                    medidor = it,
                    onDismiss = { medidorParaEliminar = null },
                    onConfirm = {
                        onDelete(it)
                        medidoresFiltrados = onSearch(searchQuery) // Refresh
                        medidorParaEliminar = null
                    }
                )
            }
        }
    }

    @Composable
    private fun FormularioMedidorContent(
        onNavigateBack: () -> Unit,
        onSave: (Medidor, String) -> Unit
    ) {
        var codigo by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf(TipoMedidor.MONOFASICO) }
        var rutCliente by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }
        var potencia by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth(0.7f).padding(top = 50.dp)) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(
                        "Registrar Nuevo Medidor",
                        style = MaterialTheme.typography.headlineMedium,
                        color = blue
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código Medidor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rutCliente,
                        onValueChange = { rutCliente = it },
                        label = { Text("RUT Cliente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección Suministro") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = potencia,
                        onValueChange = { potencia = it },
                        label = { Text("Potencia Max (kW)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SelectorTipoMedidor(tipo, onSelect = { tipo = it })


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
                                val pot = potencia.toDoubleOrNull()
                                if (codigo.isBlank() || rutCliente.isBlank() || direccion.isBlank() || pot == null) {
                                    error = "Todos los campos son obligatorios."
                                    return@Button
                                }

                                val now = Date()
                                val medidor = when (tipo) {
                                    TipoMedidor.MONOFASICO -> MedidorMonofasico(
                                        UUID.randomUUID().toString(),
                                        now,
                                        now,
                                        codigo,
                                        direccion,
                                        true,
                                        "",
                                        pot
                                    )

                                    TipoMedidor.TRIFASICO -> MedidorTrifasico(
                                        UUID.randomUUID().toString(),
                                        now,
                                        now,
                                        codigo,
                                        direccion,
                                        true,
                                        "",
                                        pot,
                                        1.0
                                    )
                                }

                                onSave(medidor, rutCliente)
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
    private fun SelectorTipoMedidor(selected: TipoMedidor, onSelect: (TipoMedidor) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedTextField(
                value = selected.str,
                onValueChange = {},
                label = { Text("Tipo de Medidor") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                TipoMedidor.values().forEach { tipo ->
                    DropdownMenuItem(text = { Text(tipo.str) }, onClick = {
                        onSelect(tipo)
                        expanded = false
                    })
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
                    Text(medidor.getCodigo(), fontWeight = FontWeight.Bold, color = blue)
                    Text("Tipo: ${medidor.tipo()}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Cliente: ${medidor.getIdCliente()}",
                        style = MaterialTheme.typography.bodySmall
                    )
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
                    Text("Código: ${medidor.getCodigo()}", fontWeight = FontWeight.Bold)
                    Text("Tipo: ${medidor.tipo()}")
                    Text("Cliente: ${medidor.getIdCliente()}")
                    Text("Dirección: ${medidor.getDireccionSuministro()}")
                    when (medidor) {
                        is MedidorMonofasico -> Text("Potencia: ${medidor.getPotenciaMaxKw()} kW")
                        is MedidorTrifasico -> {
                            Text("Potencia: ${medidor.getPotenciaMaxKw()} kW")
                            Text("Factor Potencia: ${medidor.getFactorPotencia()}")
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = onDismiss) { Text("Cerrar") } }
        )
    }

    @Composable
    private fun EliminarMedidorDialog(
        medidor: Medidor,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Seguro que deseas eliminar el medidor con código '${medidor.getCodigo()}'?") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
        )
    }
}
