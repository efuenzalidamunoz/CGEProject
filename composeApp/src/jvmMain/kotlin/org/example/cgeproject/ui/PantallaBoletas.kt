package org.example.cgeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.persistencia.BoletaRepoImpl
import org.example.cgeproject.persistencia.FileSystemStorageDriver
import org.example.cgeproject.persistencia.PersistenciaDatos
import org.example.cgeproject.servicios.BoletaService
import org.example.cgeproject.servicios.PdfService

class PantallaBoletas() {
    private val blue = Color(0xFF001689)
    private val backgroundColor = Color(0xFFF1F5FA)
    private val repo = BoletaRepoImpl(PersistenciaDatos(FileSystemStorageDriver()))

    @Composable
    fun PantallaPrincipal() {
        var idCliente by remember { mutableStateOf("") }
        var boletas by remember { mutableStateOf<List<Boleta>>(emptyList()) }
        var mostrarLista by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection()

            Spacer(modifier = Modifier.height(32.dp))

            bodyBoletas(
                idCliente = idCliente,
                onIdClienteChange = { idCliente = it },
                onSearch = {
                    boletas = repo.listarPorCliente(idCliente)
                    mostrarLista = true
                },
                onToggleLista = { mostrarLista = !mostrarLista },
                isListaVisible = mostrarLista
            )

            if (mostrarLista && idCliente.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                MostrarTablaBoletas(idCliente, boletas)
            }
        }
    }

    @Composable
    private fun HeaderSection() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(blue)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(100.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Detalle de boleta",
                    fontSize = 40.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Si tienes una consulta con el detalle de la boleta, aquí lo podrás encontrar.",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun bodyBoletas(
        idCliente: String,
        onIdClienteChange: (String) -> Unit,
        onSearch: () -> Unit,
        isListaVisible: Boolean,
        onToggleLista: () -> Unit
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
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Detalle de Boleta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = blue,
                        modifier = Modifier.padding(start = 30.dp, top = 15.dp, bottom = 15.dp)
                    )
                    Text(
                        text = "Para ver el detalle de los cargos de la boleta/factura emitida, debe ingresar el cliente que requiere consultar y finalmente el botón Ver detalle",
                        fontSize = 16.sp,
                        color = blue,
                        modifier = Modifier.padding(start = 30.dp, top = 15.dp, bottom = 30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                IngresarIdCliente(
                    idCliente = idCliente,
                    onIdClienteChange = onIdClienteChange,
                )
                Spacer(modifier = Modifier.height(50.dp))
                BotonBusqueda(
                    onSearch = onSearch,
                    isListaVisible = isListaVisible,
                    onToggleLista = onToggleLista
                )
            }
        }
    }

    @Composable
    private fun IngresarIdCliente(
        idCliente: String,
        onIdClienteChange: (String) -> Unit,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 100.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            Text(
                text = "Número de Cliente:",
                fontSize = 16.sp,
                color = blue,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = idCliente,
                onValueChange = onIdClienteChange,
                label = { Text("Ingresar el número de cliente") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = blue
                ),
                singleLine = true
            )
        }
    }

    @Composable
    private fun BotonBusqueda(
        onSearch: () -> Unit,
        isListaVisible: Boolean,
        onToggleLista: () -> Unit
    ) {
        Button(
            onClick = {
                if (isListaVisible) onToggleLista() else onSearch()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = blue
            )
        ) {
            Text(text = if (isListaVisible) "Ocultar resultados" else "Buscar")
        }
    }

    @Composable
    private fun MostrarTablaBoletas(idCliente: String, boletas: List<Boleta>) {
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(0.7f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {


                Text(
                    text = "Resultados para Cliente: $idCliente",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = blue,
                    modifier = Modifier.padding(bottom = 16.dp, start = 24.dp)
                )

                Text(
                    text = "Últimas Boletas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = blue,
                    modifier = Modifier.padding(bottom = 16.dp, start = 24.dp)
                )

                // Encabezados de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(blue.copy(alpha = 0.1f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeaderTabla("Fecha de Emisión", Modifier.weight(1.5f))
                    HeaderTabla("Consumo (kWh)", Modifier.weight(1.5f))
                    HeaderTabla("Total a Pagar ($)", Modifier.weight(1.5f))
                    HeaderTabla("Opciones", Modifier.weight(1f))
                }

                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                if (boletas.isEmpty()) {
                    Text(
                        "No se encontraron boletas para este cliente.",
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    boletas.forEach { boleta ->
                        FilasTabla(boleta, blue)
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderTabla(text: String, modifier: Modifier = Modifier) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001689),
            modifier = modifier
        )
    }

    @Composable
    private fun FilasTabla(boleta: Boleta, blue: Color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${boleta.getMes()}/${boleta.getAnio()}",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = "%.2f".format(boleta.getKwhTotal()),
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = "$%.2f".format(boleta.getDetalle().total),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = blue,
                modifier = Modifier.weight(1.5f)
            )
            Button(
                onClick = { /* Acción al presionar opciones */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blue
                )
            ) {
                Text("Ver detalle")
            }
        }
    }
}