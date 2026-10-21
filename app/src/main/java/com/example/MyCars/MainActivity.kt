package com.example.MyCars // ⚠️ Asegúrate de que coincide con tu paquete

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    // Datos en memoria
    val listaGastos = mutableListOf<Gasto>()
    val listaCoches = mutableListOf("Coche Principal")
    
    // Vistas globales
    lateinit var layoutContent: LinearLayout
    lateinit var navBtnAdd: TextView
    lateinit var navBtnSummary: TextView
    lateinit var pieChart: PieChartView

    // Colores del estilo de la foto
    val bgColor = Color.parseColor("#121212")
    val orangeColor = Color.parseColor("#FF9800")
    val cardColors = listOf("#E91E63", "#673AB7", "#3F51B5", "#009688", "#FF5722")
    var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        // Contenedor dinámico donde irán las pestañas
        layoutContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        mainLayout.addView(layoutContent)

        // Barra de navegación inferior
        val navLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(20, 20, 20, 20)
        }

        navBtnAdd = TextView(this).apply {
            text = "Nuevo Gasto"
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mostrarPantallaNuevoGasto() }
        }
        
        navBtnSummary = TextView(this).apply {
            text = "Resumen"
            setTextColor(Color.GRAY)
            textSize = 16f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mostrarPantallaResumen() }
        }

        navLayout.addView(navBtnAdd)
        navLayout.addView(navBtnSummary)
        mainLayout.addView(navLayout)

        setContentView(mainLayout)

        // Arrancamos mostrando la pantalla de añadir
        mostrarPantallaNuevoGasto()
    }

    // ==========================================
    // PANTALLA 1: NUEVO GASTO
    // ==========================================
    fun mostrarPantallaNuevoGasto() {
        layoutContent.removeAllViews()
        navBtnAdd.setTextColor(orangeColor)
        navBtnSummary.setTextColor(Color.GRAY)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        scroll.addView(layout)
        layoutContent.addView(scroll)

        val title = TextView(this).apply {
            text = "Nuevo Gasto"
            setTextColor(orangeColor)
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        // Selector de Coche
        val spinnerCoche = Spinner(this)
        val adapterCoche = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCoches)
        spinnerCoche.adapter = adapterCoche
        layout.addView(spinnerCoche)

        // Botón para añadir un nuevo coche a la lista
        val btnAddCoche = Button(this).apply {
            text = "+ Añadir Coche a la lista"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(orangeColor)
            setOnClickListener {
                val input = EditText(this@MainActivity)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Nombre del nuevo coche")
                    .setView(input)
                    .setPositiveButton("Añadir") { _, _ ->
                        val nombre = input.text.toString()
                        if (nombre.isNotBlank()) {
                            listaCoches.add(nombre)
                            adapterCoche.notifyDataSetChanged()
                            spinnerCoche.setSelection(listaCoches.size - 1)
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        layout.addView(btnAddCoche)

        // Selector de Tipo de Gasto
        val spinnerGasto = Spinner(this)
        val tipos = arrayOf("Combustible", "Mantenimiento", "Otros")
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        // Campos dinámicos
        val inputLitros = EditText(this).apply { hint = "Cantidad (Litros o kWh)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val inputKm = EditText(this).apply { hint = "Kilometraje actual (Km)"; inputType = InputType.TYPE_CLASS_NUMBER }
        val inputImporte = EditText(this).apply { hint = "Importe total (€)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }

        layout.addView(inputLitros)
        layout.addView(inputKm)
        layout.addView(inputImporte)

        // Lógica para mostrar/ocultar el campo de Litros según el tipo
        spinnerGasto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (tipos[position] == "Combustible") {
                    inputLitros.visibility = View.VISIBLE
                } else {
                    inputLitros.visibility = View.GONE
                    inputLitros.text.clear()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Botón Guardar
        val btnGuardar = Button(this).apply {
            text = "Guardar Gasto"
            setBackgroundColor(orangeColor)
            setTextColor(Color.BLACK)
            setPadding(0, 30, 0, 30)
            setOnClickListener {
                val coche = spinnerCoche.selectedItem.toString()
                val tipo = spinnerGasto.selectedItem.toString()
                val litros = inputLitros.text.toString()
                val km = inputKm.text.toString()
                val importe = inputImporte.text.toString()

                if (importe.isNotEmpty()) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    listaGastos.add(Gasto(coche, tipo, litros, km, importe, fecha, cardColors[colorIndex % cardColors.size]))
                    colorIndex++
                    Toast.makeText(this@MainActivity, "Gasto guardado", Toast.LENGTH_SHORT).show()
                    inputLitros.text.clear(); inputKm.text.clear(); inputImporte.text.clear()
                } else {
                    Toast.makeText(this@MainActivity, "El importe es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(btnGuardar)

        // Botones CSV
        val btnCSV = Button(this).apply {
            text = "Exportar a CSV"
            setOnClickListener { exportarCSV() }
        }
        layout.addView(btnCSV)
    }

    // ==========================================
    // PANTALLA 2: RESUMEN Y GRÁFICOS
    // ==========================================
    fun mostrarPantallaResumen() {
        layoutContent.removeAllViews()
        navBtnAdd.setTextColor(Color.GRAY)
        navBtnSummary.setTextColor(orangeColor)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        layoutContent.addView(layout)

        val title = TextView(this).apply {
            text = "Resumen"
            setTextColor(orangeColor)
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        layout.addView(title)

        // Selector de vehículo para el resumen
        val spinnerResumen = Spinner(this)
        val opcionesResumen = mutableListOf("Todos los coches")
        opcionesResumen.addAll(listaCoches)
        val adapterResumen = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesResumen)
        spinnerResumen.adapter = adapterResumen
        layout.addView(spinnerResumen)

        // Total Gasto
        val txtTotal = TextView(this).apply {
            text = "Total: 0 €"
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(0, 30, 0, 30)
        }
        layout.addView(txtTotal)

        // Gráfico de Tarta (Nuestro componente personalizado)
        pieChart = PieChartView(this)
        layout.addView(pieChart, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500))

        // Lista histórica
        val scrollLista = ScrollView(this)
        val listaLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scrollLista.addView(listaLayout)
        layout.addView(scrollLista)

        // Función para actualizar los datos según el coche seleccionado
        fun actualizarResumen() {
            val seleccion = spinnerResumen.selectedItem.toString()
            val gastosFiltrados = if (seleccion == "Todos los coches") listaGastos else listaGastos.filter { it.coche == seleccion }
            
            // Calcular total y datos para la tarta
            var total = 0.0
            val datosTarta = mutableMapOf<String, Float>()
            
            listaLayout.removeAllViews()

            for (g in gastosFiltrados) {
                val importe = g.importe.toDoubleOrNull() ?: 0.0
                total += importe
                datosTarta[g.tipo] = (datosTarta[g.tipo] ?: 0f) + importe.toFloat()

                // Crear la tarjeta de color estilo foto
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(30, 30, 30, 30)
                    val drawable = GradientDrawable()
                    drawable.shape = GradientDrawable.RECTANGLE
                    drawable.setColor(Color.parseColor(g.colorHex))
                    drawable.cornerRadius = 30f
                    background = drawable
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(0, 10, 0, 10)
                    }
                }

                val txtTitulo = TextView(this).apply {
                    text = "${g.coche} - ${g.tipo}"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val txtDetalle = TextView(this).apply {
                    var detalle = "${g.fecha} | ${g.importe} €"
                    if (g.litros.isNotEmpty()) detalle += " | ${g.litros} L/kWh"
                    if (g.km.isNotEmpty()) detalle += " | ${g.km} Km"
                    text = detalle
                    setTextColor(Color.WHITE)
                }

                card.addView(txtTitulo)
                card.addView(txtDetalle)
                listaLayout.addView(card)
            }

            txtTotal.text = "Total: %.2f €".format(total)
            pieChart.setData(datosTarta)
        }

        spinnerResumen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                actualizarResumen()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        actualizarResumen()
    }

    // ==========================================
    // GRÁFICO DE TARTA PERSONALIZADO (Sin librerías)
    // ==========================================
    class PieChartView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF = RectF()
        private var data: Map<String, Float> = emptyMap()
        private val colors = listOf(Color.parseColor("#E91E63"), Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"))

        fun setData(newData: Map<String, Float>) {
            data = newData
            invalidate() // Redibujar
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (data.isEmpty()) {
                paint.color = Color.GRAY
                paint.textSize = 40f
                canvas.drawText("Sin datos", width / 2f - 80, height / 2f, paint)
                return
            }

            val total = data.values.sum()
            var startAngle = -90f
            val padding = 20f
            rectF.set(padding, padding, width - padding, height - padding)

            var colorIdx = 0
            for ((tipo, valor) in data) {
                val sweepAngle = (valor / total) * 360f
                paint.color = colors[colorIdx % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
                colorIdx++
            }

            // Dibujar un círculo interior para que parezca un Donut Chart (opcional, queda más moderno)
            paint.color = Color.parseColor("#121212") // Color del fondo
            val innerRadius = (width.coerceAtMost(height) / 2f) * 0.5f
            canvas.drawCircle(width / 2f, height / 2f, innerRadius, paint)
        }
    }

    // ==========================================
    // EXPORTAR CSV
    // ==========================================
    fun exportarCSV() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "gastos_coche.csv")
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            val uri: Uri? = data?.data
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val csvBuilder = StringBuilder()
                        csvBuilder.append("Coche,Tipo,Litros,Km,Importe,Fecha\n")
                        for (gasto in listaGastos) {
                            csvBuilder.append("${gasto.coche},${gasto.tipo},${gasto.litros},${gasto.km},${gasto.importe},${gasto.fecha}\n")
                        }
                        outputStream.write(csvBuilder.toString().toByteArray())
                    }
                    Toast.makeText(this, "¡Exportado!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show()
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
    val importe: String,
    val fecha: String,
    val colorHex: String
)
