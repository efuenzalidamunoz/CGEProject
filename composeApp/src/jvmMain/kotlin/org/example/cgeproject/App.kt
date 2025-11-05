package org.example.cgeproject

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.example.cgeproject.ui.AppScreen
import org.example.cgeproject.ui.PantallaBoletas
import org.example.cgeproject.ui.PantallaClientes
import org.example.cgeproject.ui.PantallaLecturas
import org.example.cgeproject.ui.PantallaMedidores
import org.example.cgeproject.ui.components.TopNavBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var pantalla by remember { mutableStateOf(AppScreen.CLIENTES) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(
            currentScreen = pantalla,
            onScreenSelected = { screen -> pantalla = screen }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            when (pantalla) {
                AppScreen.CLIENTES -> PantallaClientes().PantallaPrincipal()
                AppScreen.MEDIDORES -> PantallaMedidores().PantallaPrincipal()
                AppScreen.LECTURAS -> PantallaLecturas().PantallaPrincipal()
                AppScreen.BOLETAS -> PantallaBoletas().PantallaPrincipal()
            }
        }
    }
}