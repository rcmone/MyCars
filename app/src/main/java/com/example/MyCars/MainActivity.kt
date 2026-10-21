package com.example.MyCars // ⚠️ Revisa que coincida con tu paquete real

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Contenedor con Scroll para que quepa todo
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }
        scrollView.addView(layout)

        // Título
        val title = TextView(this).apply {
            text = "🚗 Mi Contabilidad"
            textSize = 24f
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // 1. Campo para el Nombre del Coche (Texto libre)
        val inputCoche = EditText(this).apply {
            hint = "Nombre del coche (ej. Coche A, Furgoneta...)"
            setText("Coche Principal") // Valor por defecto
        }
        layout.addView(inputCoche)

        // 2. Selector de Tipo de Gasto
        val spinnerGasto = Spinner(this)
        val gastos = arrayOf("Combustible", "Mantenimiento", "Otros")
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_item, gastos)
        adapterGasto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        // 3. Campo para Litros o kWh
        val inputLitros = EditText(this).apply {
            hint = "Cantidad (Litros o kWh)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputLitros)

        // 4. Campo para Kilometraje
        val inputKm = EditText(this).apply {
            hint = "Kilometraje actual (Km)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(inputKm)

        // 5. Campo para el Importe Total (€)
        val inputImporte = EditText(this).apply {
            hint = "Importe total (€)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputImporte)

        // Botón para añadir
        val button = Button(this).apply {
            text = "Añadir Gasto"
            setPadding(0, 30, 0, 30)
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
            val coche = inputCoche.text.toString()
            val tipo = spinnerGasto.selectedItem.toString()
            val litros = inputLitros.text.toString()
            val km = inputKm.text.toString()
            val importe = inputImporte.text.toString()

            if (coche.isNotEmpty() && importe.isNotEmpty()) {
                // Formateamos el texto para que quede bonito
                var textoGasto = "\n- $coche | $tipo: $importe €"
                if (litros.isNotEmpty()) textoGasto += " | $litros L/kWh"
                if (km.isNotEmpty()) textoGasto += " | $km Km"
                
                textLista.append(textoGasto)
                
                // Limpiar cajas
                inputLitros.text.clear()
                inputKm.text.clear()
                inputImporte.text.clear()
            } else {
                Toast.makeText(this, "El coche y el importe son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        // Mostrar el diseño
        setContentView(scrollView)
    }
}
