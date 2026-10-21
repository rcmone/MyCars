package com.example.MyCars // ⚠️ Asegúrate de que coincide con tu paquete

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenedor principal (vertical)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        // Título
        val title = TextView(this).apply {
            text = "🚗 Mi Contabilidad"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // Selector de Coche
        val spinnerCoche = Spinner(this)
        val coches = arrayOf("Coche Principal", "Moto")
        val adapterCoche = ArrayAdapter(this, android.R.layout.simple_spinner_item, coches)
        adapterCoche.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCoche.adapter = adapterCoche
        layout.addView(spinnerCoche)

        // Selector de Tipo de Gasto
        val spinnerGasto = Spinner(this)
        val gastos = arrayOf("Combustible", "Mantenimiento", "Otros")
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_item, gastos)
        adapterGasto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        // Caja para el importe
        val inputImporte = EditText(this).apply {
            hint = "Importe (€)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputImporte)

        // Botón para añadir
        val button = Button(this).apply {
            text = "Añadir Gasto"
        }
        layout.addView(button)

        // Lista de resultados
        val textLista = TextView(this).apply {
            text = "Últimos gastos:\n"
            textSize = 16f
            setPadding(0, 40, 0, 0)
        }
        layout.addView(textLista)

        // Acción del botón
        button.setOnClickListener {
            val coche = spinnerCoche.selectedItem.toString()
            val tipo = spinnerGasto.selectedItem.toString()
            val importe = inputImporte.text.toString()

            if (importe.isNotEmpty()) {
                textLista.append("\n- $coche | $tipo: $importe €")
                inputImporte.text.clear() // Limpiar caja
            } else {
                Toast.makeText(this, "Escribe un importe", Toast.LENGTH_SHORT).show()
            }
        }

        // Mostrar el diseño
        setContentView(layout)
    }
}
