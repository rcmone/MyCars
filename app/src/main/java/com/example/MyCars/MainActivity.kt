package com.example.MyCars // ⚠️ Asegúrate de que coincide con tu paquete

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    val listaGastos = mutableListOf<Gasto>()
    val listaCoches = mutableListOf("Coche Principal")
    var colorIndex = 0

    val bgColor = Color.parseColor("#121212")
    val orangeColor = Color.parseColor("#FF9800")
    val cardColors = listOf("#E91E63", "#673AB7", "#3F51B5", "#009688", "#FF5722")

    // Vistas principales
    lateinit var rootLayout: FrameLayout
    lateinit var contentLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        rootLayout = FrameLayout(this).apply { setBackgroundColor(bgColor) }
        contentLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        
        setContentView(rootLayout)
        mostrarPantallaPrincipal()
    }

    // ==========================================
    // PANTALLA PRINCIPAL (LISTA ESTILO FOTO)
    // ==========================================
    fun mostrarPantallaPrincipal() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // --- CABECERA ---
        val header = RelativeLayout(this).apply { setPadding(50, 80, 50, 20) }
        
        val titleLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val title = TextView(this).apply {
            text = "Gastos"
            setTextColor(orangeColor)
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
        }
        val subtitle = TextView(this).apply {
            text = "Control de vencimientos"
            setTextColor(Color.LTGRAY)
            textSize = 14f
        }
        titleLayout.addView(title)
        titleLayout.addView(subtitle)
        header.addView(titleLayout)

        // Iconos de la derecha (Gráfico y Menú)
        val iconsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            layoutParams = params
        }

        // Icono Gráfico
        val iconChart = TextView(this).apply {
            text = "📊"
            textSize = 24f
            setPadding(0, 0, 30, 0)
            setOnClickListener { mostrarPantallaResumen() }
        }

        // Icono Menú (⋮)
        val iconMenu = TextView(this).apply {
            text = "⋮"
            setTextColor(Color.WHITE)
            textSize = 28f
            setPadding(0, 0, 0, 15)
            setOnClickListener { showMenu(view) }
        }

        iconsLayout.addView(iconChart)
        iconsLayout.addView(iconMenu)
        header.addView(iconsLayout)
        contentLayout.addView(header)

        // --- BUSCADOR ---
        val searchBar = EditText(this).apply {
            hint = "🔍  Buscar..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(40, 25, 40, 25)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#222222"))
                cornerRadius = 40f
            }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(50, 0, 50, 30)
            layoutParams = lp
        }
        contentLayout.addView(searchBar)

        // --- LISTA DE GASTOS ---
        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 0, 50, 150) }
        scrollView.addView(listLayout)
        contentLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        // Función para dibujar las tarjetas
        fun renderLista(filtro: String = "") {
            listLayout.removeAllViews()
            val filtrados = listaGastos.filter { it.coche.contains(filtro, true) || it.tipo.contains(filtro, true) }
            
            // Las dibujamos en orden inverso (la más reciente arriba)
            for (g in filtrados.reversed()) {
                val card = RelativeLayout(this).apply {
                    setPadding(40, 40, 40, 40)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor(g.colorHex))
                        cornerRadius = 40f
                    }
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, 0, 30)
                    layoutParams = lp
                }

                // Textos de la tarjeta
                val txtTitulo = TextView(this).apply {
                    text = "${g.coche} - ${g.tipo}"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    setTypeface(null, Typeface.BOLD)
                }
                val txtSubtitulo = TextView(this).apply {
                    var sub = ""
                    if (g.litros.isNotEmpty()) sub += "${g.litros} L/kWh | "
                    if (g.km.isNotEmpty()) sub += "${g.km} Km"
                    text = sub
                    setTextColor(Color.WHITE)
                    textSize = 14f
                }
                val txtImporte = TextView(this).apply {
                    text = "${g.importe} €"
                    setTextColor(Color.WHITE)
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                }
                val txtFecha = TextView(this).apply {
                    text = g.fecha
                    setTextColor(Color.WHITE)
                    textSize = 12f
                }

                // Posicionamiento en la tarjeta
                val lpTitulo = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_LEFT) }
                val lpImporte = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT) }
                val lpSub = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.BELOW, txtTitulo.id) }
                val lpFecha = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply { addRule(RelativeLayout.ALIGN_PARENT_RIGHT); addRule(RelativeLayout.BELOW, txtImporte.id); addRule(RelativeLayout.ALIGN_PARENT_BOTTOM) }
                
                // Asignar IDs para el posicionamiento relativo
                txtTitulo.id = View.generateViewId()
                txtImporte.id = View.generateViewId()

                card.addView(txtTitulo, lpTitulo)
                card.addView(txtImporte, lpImporte)
                card.addView(txtSubtitulo, lpSub)
                card.addView(txtFecha, lpFecha)

                listLayout.addView(card)
            }
        }

        renderLista()

        // Buscador en tiempo real
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { renderLista(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- BOTÓN FLOTANTE (NUEVO GASTO) ---
        val fab = Button(this).apply {
            text = "＋  Nuevo Gasto"
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            background = GradientDrawable().apply {
                setColor(orangeColor)
                cornerRadius = 50f
            }
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.setMargins(0, 0, 50, 50)
            layoutParams = params
            setOnClickListener { mostrarFormularioNuevoGasto() }
        }
        rootLayout.addView(fab)
    }

    // ==========================================
    // MENÚ (Importar/Exportar)
    // ==========================================
    fun showMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Exportar a CSV")
        popup.menu.add("Importar desde CSV")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Exportar a CSV" -> { exportarCSV(); true }
                "Importar desde CSV" -> { importarCSV(); true }
                else -> false
            }
        }
        popup.show()
    }

    // ==========================================
    // FORMULARIO NUEVO GASTO (PANTALLA COMPLETA)
    // ==========================================
    fun mostrarFormularioNuevoGasto() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        scroll.addView(layout)
        contentLayout.addView(scroll)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply {
            text = "← Volver"
            setTextColor(orangeColor)
            textSize = 18f
            setOnClickListener { mostrarPantallaPrincipal() }
        }
        val title = TextView(this).apply {
            text = "  Nuevo Gasto"
            setTextColor(Color.WHITE)
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerCoche = Spinner(this)
        val adapterCoche = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCoches)
        spinnerCoche.adapter = adapterCoche
        layout.addView(spinnerCoche)

        val btnAddCoche = Button(this).apply {
            text = "+ Añadir Coche a la lista"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(orangeColor)
            setOnClickListener {
                val input = EditText(this@MainActivity)
                AlertDialog.Builder(this@MainActivity).setTitle("Nombre del nuevo coche").setView(input)
                    .setPositiveButton("Añadir") { _, _ ->
                        val nombre = input.text.toString()
                        if (nombre.isNotBlank()) {
                            listaCoches.add(nombre)
                            adapterCoche.notifyDataSetChanged()
                            spinnerCoche.setSelection(listaCoches.size - 1)
                        }
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        layout.addView(btnAddCoche)

        val spinnerGasto = Spinner(this)
        val tipos = arrayOf("Combustible", "Mantenimiento", "Otros")
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        val inputLitros = EditText(this).apply { hint = "Cantidad (Litros o kWh)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
        val inputKm = EditText(this).apply { hint = "Kilometraje actual (Km)"; inputType = InputType.TYPE_CLASS_NUMBER; setTextColor(Color.WHITE) }
        val inputImporte = EditText(this).apply { hint = "Importe total (€)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }

        layout.addView(inputLitros); layout.addView(inputKm); layout.addView(inputImporte)

        spinnerGasto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                inputLitros.visibility = if (tipos[position] == "Combustible") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnGuardar = Button(this).apply {
            text = "Guardar Gasto"
            setBackgroundColor(orangeColor)
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 30, 0, 30)
            setOnClickListener {
                val importe = inputImporte.text.toString()
                if (importe.isNotEmpty()) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    listaGastos.add(Gasto(spinnerCoche.selectedItem.toString(), spinnerGasto.selectedItem.toString(), inputLitros.text.toString(), inputKm.text.toString(), importe, fecha, cardColors[colorIndex % cardColors.size]))
                    colorIndex++
                    Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
                    mostrarPantallaPrincipal()
                } else {
                    Toast.makeText(this@MainActivity, "El importe es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(btnGuardar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 40, 0, 0) })
    }

    // ==========================================
    // PANTALLA GRÁFICO (RESUMEN)
    // ==========================================
    fun mostrarPantallaResumen() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        contentLayout.addView(layout)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply {
            text = "← Volver"
            setTextColor(orangeColor)
            textSize = 18f
            setOnClickListener { mostrarPantallaPrincipal() }
        }
        val title = TextView(this).apply {
            text = "  Resumen Gráfico"
            setTextColor(Color.WHITE)
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerResumen = Spinner(this)
        val opcionesResumen = mutableListOf("Todos los coches"); opcionesResumen.addAll(listaCoches)
        val adapterResumen = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesResumen)
        spinnerResumen.adapter = adapterResumen
        layout.addView(spinnerResumen)

        val txtTotal = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 22f; setTypeface(null, Typeface.BOLD); setPadding(0, 30, 0, 30)
        }
        layout.addView(txtTotal)

        val pieChart = PieChartView(this)
        layout.addView(pieChart, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600))

        spinnerResumen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = spinnerResumen.selectedItem.toString()
                val filtrados = if (seleccion == "Todos los coches") listaGastos else listaGastos.filter { it.coche == seleccion }
                var total = 0.0
                val datosTarta = mutableMapOf<String, Float>()
                for (g in filtrados) {
                    val imp = g.importe.toDoubleOrNull() ?: 0.0
                    total += imp
                    datosTarta[g.tipo] = (datosTarta[g.tipo] ?: 0f) + imp.toFloat()
                }
                txtTotal.text = "Gasto Total: %.2f €".format(total)
                pieChart.setData(datosTarta)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    class PieChartView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rectF = RectF()
        private var data: Map<String, Float> = emptyMap()
        private val colors = listOf(Color.parseColor("#E91E63"), Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"), Color.parseColor("#009688"), Color.parseColor("#FF5722"))

        fun setData(newData: Map<String, Float>) { data = newData; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (data.isEmpty()) return
            val total = data.values.sum()
            var startAngle = -90f
            val padding = 20f
            rectF.set(padding, padding, width - padding, height - padding)

            var colorIdx = 0
            for ((_, valor) in data) {
                val sweepAngle = (valor / total) * 360f
                paint.color = colors[colorIdx % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
                colorIdx++
            }
            paint.color = Color.parseColor("#121212")
            canvas.drawCircle(width / 2f, height / 2f, (width.coerceAtMost(height) / 2f) * 0.5f, paint)
        }
    }

    // ==========================================
    // IMPORTAR / EXPORTAR CSV
    // ==========================================
    fun exportarCSV() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "text/csv"; putExtra(Intent.EXTRA_TITLE, "gastos.csv")
        }
        startActivityForResult(intent, 100)
    }

    fun importarCSV() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
        }
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (requestCode == 100 && uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        val csv = StringBuilder("Coche,Tipo,Litros,Km,Importe,Fecha\n")
                        for (g in listaGastos) csv.append("${g.coche},${g.tipo},${g.litros},${g.km},${g.importe},${g.fecha}\n")
                        out.write(csv.toString().toByteArray())
                    }
                    Toast.makeText(this, "¡Exportado!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show() }
            } else if (requestCode == 200 && uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val lines = input.bufferedReader().readLines()
                        listaGastos.clear()
                        for (i in 1 until lines.size) {
                            val p = lines[i].split(",")
                            if (p.size >= 6) listaGastos.add(Gasto(p[0], p[1], p[2], p[3], p[4], p[5], cardColors[colorIndex++ % cardColors.size]))
                        }
                        mostrarPantallaPrincipal() // Refrescar
                    }
                    Toast.makeText(this, "¡Importado!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error al importar", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}

data class Gasto(val coche: String, val tipo: String, val litros: String, val km: String, val importe: String, val fecha: String, val colorHex: String)
