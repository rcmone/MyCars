package com.example.MyCars // ⚠️ Revisa que coincida con tu paquete real

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.*
import java.io.InputStream

class MainActivity : Activity() {

    // Lista en memoria para guardar los gastos
    val listaGastos = mutableListOf<Gasto>()
    lateinit var textLista: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }
        scrollView.addView(layout)

        val title = TextView(this).apply {
            text = "🚗 Mi Contabilidad"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // Campos de entrada
        val inputCoche = EditText(this).apply { hint = "Nombre del coche (ej. Coche A)"; setText("Coche Principal") }
        layout.addView(inputCoche)

        val spinnerGasto = Spinner(this)
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Combustible", "Mantenimiento", "Otros"))
        adapterGasto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        val inputLitros = EditText(this).apply { hint = "Cantidad (Litros o kWh)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        layout.addView(inputLitros)

        val inputKm = EditText(this).apply { hint = "Kilometraje actual (Km)"; inputType = InputType.TYPE_CLASS_NUMBER }
        layout.addView(inputKm)

        val inputImporte = EditText(this).apply { hint = "Importe total (€)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        layout.addView(inputImporte)

        // Botón Añadir
        val button = Button(this).apply { text = "Añadir Gasto"; setPadding(0, 30, 0, 30) }
        layout.addView(button)

        // Botones de Importar / Exportar
        val buttonExport = Button(this).apply { text = "📤 Exportar a CSV" }
        layout.addView(buttonExport)

        val buttonImport = Button(this).apply { text = "📥 Importar desde CSV" }
        layout.addView(buttonImport)

        textLista = TextView(this).apply { text = "Últimos gastos:\n"; textSize = 16f; setPadding(0, 40, 0, 0) }
        layout.addView(textLista)

        // ACCIÓN: Añadir gasto
        button.setOnClickListener {
            val coche = inputCoche.text.toString()
            val tipo = spinnerGasto.selectedItem.toString()
            val litros = inputLitros.text.toString()
            val km = inputKm.text.toString()
            val importe = inputImporte.text.toString()

            if (coche.isNotEmpty() && importe.isNotEmpty()) {
                listaGastos.add(Gasto(coche, tipo, litros, km, importe))
                actualizarListaUI()
                inputLitros.text.clear(); inputKm.text.clear(); inputImporte.text.clear()
            } else {
                Toast.makeText(this, "El coche y el importe son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        // ACCIÓN: Exportar CSV
        buttonExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "gastos_coche.csv")
            }
            startActivityForResult(intent, 100)
        }

        // ACCIÓN: Importar CSV
        buttonImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Permite buscar cualquier archivo
            }
            startActivityForResult(intent, 200)
        }

        setContentView(scrollView)
    }

    // Función para refrescar la lista en pantalla
    fun actualizarListaUI() {
        val builder = StringBuilder("Últimos gastos:\n")
        for (g in listaGastos) {
            var linea = "\n- ${g.coche} | ${g.tipo}: ${g.importe} €"
            if (g.litros.isNotEmpty()) linea += " | ${g.litros} L/kWh"
            if (g.km.isNotEmpty()) linea += " | ${g.km} Km"
            builder.append(linea)
        }
        textLista.text = builder.toString()
    }

    // Función que recibe el archivo seleccionado por el usuario
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (requestCode == 100 && uri != null) {
                // EXPORTAR
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val csvBuilder = StringBuilder()
                        csvBuilder.append("Coche,Tipo,Litros,Km,Importe\n") // Cabecera
                        for (gasto in listaGastos) {
                            csvBuilder.append("${gasto.coche},${gasto.tipo},${gasto.litros},${gasto.km},${gasto.importe}\n")
                        }
                        outputStream.write(csvBuilder.toString().toByteArray())
                    }
                    Toast.makeText(this, "¡Exportado con éxito!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == 200 && uri != null) {
                // IMPORTAR
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = inputStream.bufferedReader()
                        val lines = reader.readLines()
                        listaGastos.clear() // Limpiamos lo que hubiera
                        
                        // Saltamos la primera línea (la cabecera) y leemos el resto
                        for (i in 1 until lines.size) {
                            val parts = lines[i].split(",")
                            if (parts.size >= 5) {
                                listaGastos.add(Gasto(parts[0], parts[1], parts[2], parts[3], parts[4]))
                            }
                        }
                        actualizarListaUI()
                    }
                    Toast.makeText(this, "¡Importado con éxito!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al importar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Estructura de datos
data class Gasto(
    val coche: String,
    val tipo: String,
    val litros: String,
    val km: String,
    val importe: String
)
