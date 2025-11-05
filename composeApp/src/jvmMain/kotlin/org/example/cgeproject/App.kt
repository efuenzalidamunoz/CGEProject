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
import org.example.cgeproject.dominio.Boleta
import org.example.cgeproject.dominio.Cliente
import org.example.cgeproject.dominio.EstadoBoleta
import org.example.cgeproject.dominio.EstadoCliente
import org.example.cgeproject.dominio.LecturaConsumo
import org.example.cgeproject.dominio.MedidorMonofasico
import org.example.cgeproject.dominio.MedidorTrifasico
import org.example.cgeproject.dominio.TarifaDetalle
import org.example.cgeproject.persistencia.BoletaRepoImpl
import org.example.cgeproject.persistencia.ClienteRepoImpl
import org.example.cgeproject.persistencia.FileSystemStorageDriver
import org.example.cgeproject.persistencia.LecturaRepoImpl
import org.example.cgeproject.persistencia.MedidorRepoImpl
import org.example.cgeproject.persistencia.PersistenciaDatos
import org.example.cgeproject.ui.AppScreen
import org.example.cgeproject.ui.PantallaBoletas
import org.example.cgeproject.ui.PantallaClientes
import org.example.cgeproject.ui.PantallaLecturas
import org.example.cgeproject.ui.PantallaMedidores
import org.example.cgeproject.ui.components.TopNavBar
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.Date

@Composable
@Preview
fun App() {
//    var pantalla by remember { mutableStateOf(AppScreen.CLIENTES) }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        TopNavBar(
//            currentScreen = pantalla,
//            onScreenSelected = { screen -> pantalla = screen }
//        )
//        Box(modifier = Modifier.fillMaxSize()) {
//            when (pantalla) {
//                AppScreen.CLIENTES -> PantallaClientes().PantallaPrincipal()
//                AppScreen.MEDIDORES -> PantallaMedidores().PantallaPrincipal()
//                AppScreen.LECTURAS -> PantallaLecturas().PantallaPrincipal()
//                AppScreen.BOLETAS -> PantallaBoletas().PantallaPrincipal()
//            }
//        }
//    }

    // 1) Setup driver / persistencia / repos
    val driver = FileSystemStorageDriver()
    val persist = PersistenciaDatos(driver)

    val clienteRepo = ClienteRepoImpl(persist)
    val medidorRepo = MedidorRepoImpl(persist)
    val lecturaRepo = LecturaRepoImpl(persist)
    val boletaRepo = BoletaRepoImpl(persist)

    println("Data path: ${driver.getDataPath()}")
    println("Archivos actuales: ${persist.keys("").joinToString(", ")}")

    // 2) Limpiar datos para la demo (sobrescribe los headers)
    println("Limpieza realizada. Archivos: ${persist.keys("").joinToString(", ")}")

    // 3) Crear cliente
    val now = Date()
    val cliente = Cliente(
        rut = "12345678-9",
        nombre = "Juan Pérez",
        email = "juan.perez@example.com",
        direccionFacturacion = "Av. Siempre Viva 742",
        estado = EstadoCliente.ACTIVO,             // asumo enum EstadoCliente
        boletas = mutableListOf(),
        medidores = mutableListOf()
    )

    val creado = clienteRepo.crear(cliente)
    println("Cliente creado: ${creado.getRut()} - ${creado.getNombre()}")

    // 4) Crear medidores y asociar al cliente
    val med1 = MedidorMonofasico(
        id = "MED-001",
        createdAt = now,
        updatedAt = now,
        codigo = "COD-MED-001",
        direccionSuministro = "Av. Siempre Viva 742",
        activo = true,
        idCliente = "12345678-9",
        potenciaMaxKw = 5.0
    )

    val med2 = MedidorTrifasico(
        id = "MED-002",
        createdAt = now,
        updatedAt = now,
        codigo = "COD-MED-002",
        direccionSuministro = "Av. Siempre Viva 742",
        activo = true,
        idCliente = "12345678-9",
        potenciaMaxKw = 15.0,
        factorPotencia = 0.95
    )

    medidorRepo.crear(med1, "12345678-9")
    medidorRepo.crear(med2, "12345678-9")
    println("Medidores creados para cliente 12345678-9: ${medidorRepo.listarPorCliente("12345678-9").map { it.getCodigo() }}")

    // 5) Registrar lecturas
    val lectura1 = LecturaConsumo(
        "L-001",
        now,
        now,
        idMedidor = "MED-001",
        anio = 2024,
        mes = 10,
        kwhLeidos = 250.5
    )
    val lectura2 = LecturaConsumo("L-002", now, now, idMedidor = "MED-001", anio = 2024, mes = 11, kwhLeidos = 270.0)
    val lectura3 = LecturaConsumo("L-003", now, now, idMedidor = "MED-002", anio = 2024, mes = 10, kwhLeidos = 450.0)

    lecturaRepo.registrar(lectura1)
    lecturaRepo.registrar(lectura2)
    lecturaRepo.registrar(lectura3)

    println("Lecturas MED-001 (Oct 2024): ${lecturaRepo.listarPorMedidorMes("MED-001", 2024, 10).map { it.getKwhLeidos() }}")
    println("Última lectura MED-001: ${lecturaRepo.ultimaLectura("MED-001")?.let { it.getKwhLeidos() to it.getCreatedAt() }}")

    // 6) Emitir boletas
    val detalle = TarifaDetalle(
        kwh = 100.0,
        subtotal = 250.5 * 120.0,
        cargos = 3500.0,
        iva = (250.5 * 120.0 + 3500.0) * 0.19,
        total = 0.0
    ).also {
        // recomputar total
        it.total = it.subtotal + it.cargos + it.iva
    }

    // Nota: si tu TarifaDetalle es data class inmutable, crea con total ya calculado:
    val detalleBoleta = TarifaDetalle(
        kwh = 100.0,
        subtotal = 250.5 * 120.0,
        cargos = 3500.0,
        iva = (250.5 * 120.0 + 3500.0) * 0.19,
        total = (250.5 * 120.0) + 3500.0 + (250.5 * 120.0 + 3500.0) * 0.19
    )

    val boleta = Boleta(
        id = "BOL-001",
        createdAt = now,
        updatedAt = now,
        idCliente = "12345678-9",
        anio = 2024,
        mes = 10,
        kwhTotal = 250.5,
        detalle = detalleBoleta,
        estado = EstadoBoleta.PENDIENTE
    )

    boletaRepo.guardar(boleta)
    println("Boleta guardada: ${boleta.getIdCliente()} ${boleta.getAnio()}/${boleta.getMes()} -> ${String.format("%.2f", boleta.getDetalle().total)}")

    // 7) Consultas de ejemplo
    val encontrada = boletaRepo.obtener("12345678-9", 2024, 10)
    println("Boleta obtenida por rut/anio/mes: ${encontrada?.getIdCliente()} ${encontrada?.getAnio()}/${encontrada?.getMes()} -> ${encontrada?.getDetalle()?.total}")

    val boletasCliente = boletaRepo.listarPorCliente("12345678-9")
    println("Boletas de cliente: ${boletasCliente.map { it.getId() to "${it.getAnio()}/${it.getMes()}" }}")

    val clientes = clienteRepo.listar()
    println("Clientes registrados: ${clientes.map { it.getRut() + " - " + it.getNombre() }}")
}
