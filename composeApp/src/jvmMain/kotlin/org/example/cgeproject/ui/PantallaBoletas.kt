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
        var factorPotencia by remember { mutableStateOf("") }
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

                    // Nuevo campo para factor de potencia (solo relevante para trifásico)
                    OutlinedTextField(
                        value = factorPotencia,
                        onValueChange = { factorPotencia = it },
                        label = { Text("Factor de Potencia (ej. 0.95) - solo Trifásico") },
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
                                val factor = factorPotencia.toDoubleOrNull()

                                if (codigo.isBlank() || rutCliente.isBlank() || direccion.isBlank() || pot == null) {
                                    error = "Todos los campos obligatorios deben completarse y la potencia debe ser un número."
                                    return@Button
                                }

                                if (tipo == TipoMedidor.TRIFASICO && factor == null) {
                                    error = "Para medidor Trifásico, ingrese un factor de potencia válido."
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
                                        factor ?: 1.0
                                    )
                                }

                                error = null
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
