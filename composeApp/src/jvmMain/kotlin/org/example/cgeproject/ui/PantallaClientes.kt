package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.EstadoCliente
import org.example.cgeproject.persistencia.ClienteRepoImpl
import org.example.cgeproject.persistencia.FileSystemStorageDriver
import org.example.cgeproject.persistencia.PersistenciaDatos

// Enum para controlar qué pantalla se muestra
private enum class Pantalla {
    LISTA,
    FORMULARIO
}

class PantallaClientes {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)

    // --- Repositorio ---
    private val repo = ClienteRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))

    @Composable
    fun PantallaPrincipal() {
        var pantallaActual by remember { mutableStateOf(Pantalla.LISTA) }
        var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
        var clientes by remember { mutableStateOf(repo.listar()) }


        when (pantallaActual) {
            Pantalla.LISTA -> {
                GestionClientesContent(
                    clientes = clientes,
                    onAddCliente = {
                        clienteSeleccionado = null
                        pantallaActual = Pantalla.FORMULARIO
                    },
                    onEditCliente = { cliente ->
                        clienteSeleccionado = cliente
                        pantallaActual = Pantalla.FORMULARIO
                    },
                    onDeleteCliente = { cliente ->
                        repo.eliminar(cliente.getRut())
                        clientes = repo.listar() // refresh
                    }
                )
            }

            Pantalla.FORMULARIO -> {
                RegisterOrEditContent(
                    cliente = clienteSeleccionado,
                    onNavigateBack = { pantallaActual = Pantalla.LISTA },
                    onSaveCliente = { clienteGuardado ->
                        if (clienteSeleccionado == null) {
                            repo.crear(clienteGuardado)
                        } else {
                            repo.actualizar(clienteGuardado)
                        }
                        clientes = repo.listar() // refresh
                        pantallaActual = Pantalla.LISTA // Vuelve a la lista después de guardar
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GestionClientesContent(
        clientes: List<Cliente>,
        onAddCliente: () -> Unit,
        onEditCliente: (Cliente) -> Unit,
        onDeleteCliente: (Cliente) -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var clienteParaEliminar by remember { mutableStateOf<Cliente?>(null) }

        val clientesFiltrados = if (searchQuery.isBlank()) {
            clientes
        } else {
            clientes.filter {
                it.getNombre().contains(searchQuery, ignoreCase = true) ||
                        it.getRut().contains(searchQuery, ignoreCase = true)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gestión de Clientes", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = blue)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddCliente, containerColor = blue) {
                    Text("+", fontSize = 24.sp, color = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().background(backgroundColor).padding(paddingValues).fillMaxHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth(0.7f),
                    label = { Text("Buscar por Nombre o RUT") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = blue),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.7f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clientesFiltrados) { cliente ->
                        ClienteItem(
                            cliente = cliente,
                            onEdit = { onEditCliente(cliente) },
                            onDelete = { clienteParaEliminar = cliente }
                        )
                    }
                }
            }

            clienteParaEliminar?.let { cliente ->
                DeleteConfirmationDialog(
                    cliente = cliente,
                    onConfirm = {
                        onDeleteCliente(cliente)
                        clienteParaEliminar = null
                    },
                    onDismiss = { clienteParaEliminar = null }
                )
            }
        }
    }

    @Composable
    private fun RegisterOrEditContent(
        cliente: Cliente?,
        onNavigateBack: () -> Unit,
        onSaveCliente: (Cliente) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(backgroundColor)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            RegisterCard(
                clienteAEditar = cliente,
                onNavigateBack = onNavigateBack,
                onSave = onSaveCliente
            )
        }
    }

    @Composable
    private fun RegisterCard(
        clienteAEditar: Cliente?,
        onNavigateBack: () -> Unit,
        onSave: (Cliente) -> Unit
    ) {
        var rut by remember { mutableStateOf(clienteAEditar?.getRut() ?: "") }
        var nombre by remember { mutableStateOf(clienteAEditar?.getNombre() ?: "") }
        var email by remember { mutableStateOf(clienteAEditar?.getEmail() ?: "") }
        var direccionFacturacion by remember {
            mutableStateOf(
                clienteAEditar?.getDireccionFacturacion() ?: ""
            )
        }
        var estado by remember {
            mutableStateOf(
                clienteAEditar?.getEstado() ?: EstadoCliente.INACTIVO
            )
        }
        var error by remember { mutableStateOf<String?>(null) }

        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(24.dp).fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 50.dp, top = 30.dp, end = 30.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (clienteAEditar == null) "Registro de Cliente" else "Editar Cliente",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = blue,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                CampoRegistroCliente(
                    rut,
                    onChange = { rut = it },
                    label = "Rut",
                    enabled = clienteAEditar == null
                )
                CampoRegistroCliente(nombre, onChange = { nombre = it }, label = "Nombre")
                CampoRegistroCliente(email, onChange = { email = it }, label = "Email")
                CampoRegistroCliente(
                    direccionFacturacion,
                    onChange = { direccionFacturacion = it },
                    label = "Dirección de Facturación"
                )
                SelectorEstadoCliente(selectedState = estado, onStateSelected = { estado = it })

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = {
                            if (rut.isBlank() || nombre.isBlank() || email.isBlank() || direccionFacturacion.isBlank()) {
                                error = "Todos los campos son obligatorios"
                                return@Button
                            }
                            val esCreacion = clienteAEditar == null
                            if (esCreacion && repo.listar().any { it.getRut() == rut }) {
                                error = "Ya existe un cliente con ese RUT"
                                return@Button
                            }
                            onSave(Cliente(rut, nombre, email, direccionFacturacion, estado))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) {
                        Text("Guardar Cliente")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onNavigateBack) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    @Composable
    private fun ClienteItem(cliente: Cliente, onEdit: () -> Unit, onDelete: () -> Unit) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cliente.getNombre(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = blue
                    )
                    Text("RUT: ${cliente.getRut()}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Estado: ${cliente.getEstado()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cliente.getEstado() == EstadoCliente.ACTIVO) Color(0xFF008000) else Color.Gray
                    )
                }
                Row {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) { Text("Editar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                }
            }
        }
    }

    @Composable
    private fun DeleteConfirmationDialog(
        cliente: Cliente,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar al cliente '${cliente.getNombre()}'?") },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
        )
    }

    @Composable
    private fun CampoRegistroCliente(
        value: String,
        onChange: (String) -> Unit,
        label: String,
        enabled: Boolean = true
    ) {
        Text(text = "$label:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(60.dp).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = blue),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    @Composable
    private fun SelectorEstadoCliente(
        selectedState: EstadoCliente,
        onStateSelected: (EstadoCliente) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        Text(text = "Estado:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue)
        Box {
            OutlinedTextField(
                value = selectedState.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(60.dp).fillMaxWidth().clickable { expanded = true },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = blue)
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                EstadoCliente.values().forEach { estado ->
                    DropdownMenuItem(text = { Text(estado.name) }, onClick = {
                        onStateSelected(estado)
                        expanded = false
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}