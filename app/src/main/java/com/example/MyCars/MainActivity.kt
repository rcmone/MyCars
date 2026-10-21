package com.example.MyCars // ⚠️ Asegúrate de que esto coincide con tu paquete

import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Typeface

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creamos el diseño directamente en código
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        val title = TextView(this).apply {
            text = "🚗 Mi Contabilidad"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        val input = EditText(this).apply {
            hint = "Importe del gasto (€)"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        layout.addView(input)

        val button = Button(this).apply {
            text = "Añadir Gasto"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 30 }
        }
        layout.addView(button)

        val listText = TextView(this).apply {
            text = "Últimos gastos:\n"
            textSize = 16f
            setPadding(0, 50, 0, 0)
        }
        layout.addView(listText)

        var totalGastos = 0.0

        button.setOnClickListener {
            val amountStr = input.text.toString()
            if (amountStr.isNotEmpty()) {
                try {
                    val amount = amountStr.toDouble()
                    totalGastos += amount
                    listText.append("\n- Gasto: $amount € (Total: $totalGastos €)")
                    input.text.clear()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContentView(layout)
    }
}
