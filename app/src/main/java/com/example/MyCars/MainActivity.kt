package com.example.contabilidadcoches

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContabilidadApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContabilidadApp() {
    // Estados para los datos
    var cocheSeleccionado by remember { mutableStateOf("Coche Principal") }
    var tipoGasto by remember { mutableStateOf("Combustible") }
    var cantidad by remember { mutableStateOf("") }
    var listaGastos by remember { mutableStateOf(listOf<Gasto>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🚗 Mi Contabilidad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Selector de Coche
        Text("Vehículo:", fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Coche Principal", "Moto").forEach { coche ->
                FilterChip(
                    selected = cocheSeleccionado == coche,
                    onClick = { cocheSeleccionado = coche },
                    label = { Text(coche) }
                )
            }
        }

        // Selector de Tipo de Gasto
        Text("Tipo de gasto:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Combustible", "Mantenimiento", "Otros").forEach { tipo ->
                FilterChip(
                    selected = tipoGasto == tipo,
                    onClick = { tipoGasto = tipo },
                    label = { Text(tipo) }
                )
            }
        }

        // Campo de texto para el importe
        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Importe (€) o Litros") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        // Botón para guardar
        Button(
            onClick = {
                if (cantidad.isNotBlank()) {
                    val nuevoGasto = Gasto(cocheSeleccionado, tipoGasto, cantidad)
                    listaGastos = listaGastos + nuevoGasto
                    cantidad = "" // Limpiar
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text("Añadir Gasto")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Lista de gastos
        Text("Últimos gastos:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaGastos) { gasto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${gasto.tipo} - ${gasto.coche}", fontWeight = FontWeight.Bold)
                        Text("${gasto.cantidad} €")
                    }
                }
            }
        }
    }
}

// Estructura para guardar cada gasto
data class Gasto(
    val coche: String,
    val tipo: String,
    val cantidad: String
)