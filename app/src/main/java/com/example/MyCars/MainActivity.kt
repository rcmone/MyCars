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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    val listaGastos = mutableListOf<Gasto>()
    val listaCoches = mutableListOf("Coche Principal")
    var colorIndex = 0

    val bgColor = Color.parseColor("#121212")
    val orangeColor = Color.parseColor("#FF9800")
    val cardColors = listOf("#E91E63", "#673AB7", "#3F51B5", "#009688", "#FF5722")

    lateinit var rootLayout: FrameLayout
    lateinit var contentLayout: LinearLayout

    var uriSeleccionada: Uri? = null
    var txtArchivoGlobal: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. OCULTAR LA BARRA SUPERIOR DEL SISTEMA (La que pone MyCars en gris)
        actionBar?.hide()

        rootLayout = FrameLayout(this).apply { setBackgroundColor(bgColor) }
        contentLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        
        setContentView(rootLayout)
        
        cargarDatos()
        mostrarPantallaPrincipal()
    }

    // ==========================================
    // GUARDAR Y CARGAR DATOS (PERSISTENCIA)
    // ==========================================
    fun guardarDatos() {
        try {
            val jsonArray = JSONArray()
            for (g in listaGastos) {
                val obj = JSONObject()
                obj.put("coche", g.coche)
                obj.put("tipo", g.tipo)
                obj.put("litros", g.litros)
                obj.put("km", g.km)
                obj.put("importe", g.importe)
                obj.put("fecha", g.fecha)
                obj.put("colorHex", g.colorHex)
                obj.put("depositoLleno", g.depositoLleno)
                obj.put("observaciones", g.observaciones)
                obj.put("documentoUri", g.documentoUri ?: "")
                jsonArray.put(obj)
            }
            openFileOutput("gastos.json", MODE_PRIVATE).use { stream ->
                stream.write(jsonArray.toString().toByteArray())
            }
            
            val cochesArray = JSONArray()
            for (c in listaCoches) cochesArray.put(c)
            openFileOutput("coches.json", MODE_PRIVATE).use { stream ->
                stream.write(cochesArray.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cargarDatos() {
        try {
            val jsonString = openFileInput("coches.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            listaCoches.clear()
            for (i in 0 until jsonArray.length()) {
                listaCoches.add(jsonArray.getString(i))
            }
        } catch (e: java.io.FileNotFoundException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val jsonString = openFileInput("gastos.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            listaGastos.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                listaGastos.add(
                    Gasto(
                        coche = obj.getString("coche"),
                        tipo = obj.getString("tipo"),
                        litros = obj.optString("litros", ""),
                        km = obj.optString("km", ""),
                        importe = obj.getString("importe"),
                        fecha = obj.getString("fecha"),
                        colorHex = obj.getString("colorHex"),
                        depositoLleno = obj.optBoolean("depositoLleno", false),
                        observaciones = obj.optString("observaciones", ""),
                        documentoUri = obj.optString("documentoUri").takeIf { it.isNotEmpty() }
                    )
                )
            }
            colorIndex = listaGastos.size
        } catch (e: java.io.FileNotFoundException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // PANTALLA PRINCIPAL
    // ==========================================
    fun mostrarPantallaPrincipal() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val header = RelativeLayout(this).apply { setPadding(50, 80, 50, 20) }
        
        // 2. TÍTULO CENTRADO Y EN NARANJA (Sin subtítulo)
        val title = TextView(this).apply {
            text = "My Cars"
            setTextColor(orangeColor)
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            // Centramos el título horizontalmente
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams = params
        }
        header.addView(title)

        // ICONOS A LA DERECHA
        val iconsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            layoutParams = params
        }

        val iconStats = TextView(this).apply {
            text = "📈"
            textSize = 24f
            setPadding(0, 0, 30, 0)
            setOnClickListener { mostrarPantallaEstadisticas() }
        }

        val iconChart = TextView(this).apply {
            text = "📊"
            textSize = 24f
            setPadding(0, 0, 30, 0)
            setOnClickListener { mostrarPantallaResumen() }
        }

        val iconMenu = TextView(this).apply {
            text = "⋮"
            setTextColor(Color.WHITE)
            textSize = 28f
            setPadding(0, 0, 0, 15)
            setOnClickListener { v -> showMenu(v) }
        }

        iconsLayout.addView(iconStats)
        iconsLayout.addView(iconChart)
        iconsLayout.addView(iconMenu)
        header.addView(iconsLayout)
        contentLayout.addView(header)

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

        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 0, 50, 150) }
        scrollView.addView(listLayout)
        contentLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        fun renderLista(filtro: String = "") {
            listLayout.removeAllViews()
            val filtrados = listaGastos.filter { it.coche.contains(filtro, true) || it.tipo.contains(filtro, true) }
            
            for (g in filtrados.reversed()) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(40, 40, 40, 40)
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor(g.colorHex))
                        cornerRadius = 40f
                    }
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, 0, 30)
                    layoutParams = lp
                }

                val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                val txtTitulo = TextView(this).apply {
                    text = "${g.coche} - ${g.tipo}"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val txtImporte = TextView(this).apply {
                    text = "${g.importe} €"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 0, 20, 0)
                }
                
                val btnDelete = TextView(this).apply {
                    text = "🗑️"
                    textSize = 20f
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Eliminar gasto")
                            .setMessage("¿Estás seguro de que quieres borrar este gasto?")
                            .setPositiveButton("Sí") { _, _ ->
                                listaGastos.remove(g)
                                guardarDatos()
                                renderLista(searchBar.text.toString())
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }

                topRow.addView(txtTitulo)
                topRow.addView(txtImporte)
                topRow.addView(btnDelete)

                val botRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 10, 0, 0) }
                val txtSubtitulo = TextView(this).apply {
                    var sub = ""
                    if (g.litros.isNotEmpty()) sub += "${g.litros} L/kWh  "
                    if (g.km.isNotEmpty()) sub += "${g.km} Km"
                    if (g.depositoLleno) sub += " (Lleno)"
                    text = sub
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val txtFecha = TextView(this).apply {
                    text = g.fecha
                    setTextColor(Color.WHITE)
                    textSize = 14f
                }
                botRow.addView(txtSubtitulo)
                botRow.addView(txtFecha)

                card.addView(topRow)
                card.addView(botRow)

                if (g.observaciones.isNotEmpty()) {
                    val txtObs = TextView(this).apply {
                        text = "📝 ${g.observaciones}"
                        setTextColor(Color.WHITE)
                        textSize = 14f
                        setPadding(0, 10, 0, 0)
                    }
                    card.addView(txtObs)
                }

                if (g.documentoUri != null) {
                    val txtDoc = TextView(this).apply {
                        text = "📎 Ver documento adjunto"
                        setTextColor(Color.parseColor("#FFD54F"))
                        textSize = 14f
                        setPadding(0, 10, 0, 0)
                        setOnClickListener {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(g.documentoUri))
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "No se puede abrir el archivo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    card.addView(txtDoc)
                }

                listLayout.addView(card)
            }
        }

        renderLista()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { renderLista(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val fab = Button(this).apply {
            text = "＋ Añadir"
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
    // FORMULARIO NUEVO GASTO
    // ==========================================
    fun mostrarFormularioNuevoGasto() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        uriSeleccionada = null

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

        val btnEditCoches = Button(this).apply {
            text = "✏️ Editar nombres de coches"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(orangeColor)
            setOnClickListener {
                val dialogLayout = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 30, 50, 30) }
                val scrollDialog = ScrollView(this@MainActivity); scrollDialog.addView(dialogLayout)
                val editTexts = mutableListOf<EditText>()

                fun agregarCampo(nombre: String) {
                    val et = EditText(this@MainActivity).apply {
                        setText(nombre); setTextColor(Color.WHITE); setHintTextColor(Color.GRAY); hint = "Nombre del coche"
                    }
                    editTexts.add(et); dialogLayout.addView(et)
                }
                listaCoches.forEach { agregarCampo(it) }
                dialogLayout.addView(Button(this@MainActivity).apply { text = "+ Añadir otro coche"; setOnClickListener { agregarCampo("") } })

                AlertDialog.Builder(this@MainActivity).setTitle("Configurar nombres").setView(scrollDialog)
                    .setPositiveButton("Guardar") { _, _ ->
                        listaCoches.clear()
                        for (et in editTexts) {
                            val nombre = et.text.toString().trim()
                            if (nombre.isNotEmpty() && !listaCoches.contains(nombre)) listaCoches.add(nombre)
                        }
                        if (listaCoches.isEmpty()) listaCoches.add("Coche Principal")
                        spinnerCoche.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listaCoches)
                        guardarDatos()
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        layout.addView(btnEditCoches)

        val spinnerGasto = Spinner(this)
        val tipos = arrayOf("Combustible", "Mantenimiento", "Otros")
        val adapterGasto = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        spinnerGasto.adapter = adapterGasto
        layout.addView(spinnerGasto)

        val inputImporte = EditText(this).apply { hint = "Importe total (€)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
        layout.addView(inputImporte)

        val inputLitros = EditText(this).apply { hint = "Cantidad (Litros o kWh)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
        val inputKm = EditText(this).apply { hint = "Kilometraje actual (Km)"; inputType = InputType.TYPE_CLASS_NUMBER; setTextColor(Color.WHITE) }
        val checkLleno = CheckBox(this).apply {
            text = "Depósito lleno"
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(orangeColor)
        }
        layout.addView(inputLitros)
        layout.addView(checkLleno)
        layout.addView(inputKm)

        val layoutExtras = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        
        val inputObservaciones = EditText(this).apply { hint = "Observaciones (ej. Cambio de aceite)"; setTextColor(Color.WHITE) }
        
        val btnAdjuntar = Button(this).apply {
            text = "📎 Adjuntar documento o foto"
            setBackgroundColor(Color.parseColor("#333333"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, 300)
            }
        }
        
        val txtArchivoSeleccionado = TextView(this).apply { 
            text = "Ningún archivo seleccionado"
            setTextColor(Color.LTGRAY)
            setPadding(0, 10, 0, 10)
        }

        layoutExtras.addView(inputObservaciones)
        layoutExtras.addView(btnAdjuntar)
        layoutExtras.addView(txtArchivoSeleccionado)
        layout.addView(layoutExtras)

        this.txtArchivoGlobal = txtArchivoSeleccionado

        spinnerGasto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (tipos[position] == "Combustible") {
                    inputLitros.visibility = View.VISIBLE
                    checkLleno.visibility = View.VISIBLE
                    inputKm.visibility = View.VISIBLE
                    layoutExtras.visibility = View.GONE
                } else {
                    inputLitros.visibility = View.GONE
                    checkLleno.visibility = View.GONE
                    inputKm.visibility = View.VISIBLE
                    layoutExtras.visibility = View.VISIBLE
                }
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
                val coche = spinnerCoche.selectedItem.toString()
                val importe = inputImporte.text.toString()
                
                if (importe.isNotEmpty()) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    listaGastos.add(Gasto(
                        coche = coche,
                        tipo = spinnerGasto.selectedItem.toString(),
                        litros = inputLitros.text.toString(),
                        km = inputKm.text.toString(),
                        importe = importe,
                        fecha = fecha,
                        colorHex = cardColors[colorIndex % cardColors.size],
                        depositoLleno = checkLleno.isChecked,
                        observaciones = inputObservaciones.text.toString(),
                        documentoUri = uriSeleccionada?.toString()
                    ))
                    colorIndex++
                    guardarDatos()
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
    // PANTALLA ESTADÍSTICAS
    // ==========================================
    fun mostrarPantallaEstadisticas() {
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
            text = "  Estadísticas Combustible"
            setTextColor(Color.WHITE)
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
        }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerStats = Spinner(this)
        val opciones = mutableListOf("Todos los coches"); opciones.addAll(listaCoches.distinct())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        spinnerStats.adapter = adapter
        layout.addView(spinnerStats)

        val statsLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 30, 0, 0) }
        layout.addView(statsLayout)

        fun calcularStats(cocheSeleccionado: String) {
            statsLayout.removeAllViews()
            val repostajes = listaGastos.filter { 
                (cocheSeleccionado == "Todos los coches" || it.coche == cocheSeleccionado) &&
                it.tipo == "Combustible" && it.litros.isNotEmpty() && it.km.isNotEmpty() 
            }.sortedBy { it.km.toIntOrNull() ?: 0 }

            if (repostajes.size < 2) {
                statsLayout.addView(TextView(this).apply { text = "Necesitas al menos 2 repostajes con Km."; setTextColor(Color.LTGRAY) })
                return
            }

            var totalLitros = 0.0; var totalImporte = 0.0
            var consumos = mutableListOf<Double>()
            var litrosAcumulados = 0.0; var ultimoKmLleno = repostajes.first().km.toIntOrNull() ?: 0

            for (i in repostajes.indices) {
                val r = repostajes[i]
                val litros = r.litros.toDoubleOrNull() ?: 0.0
                val importe = r.importe.toDoubleOrNull() ?: 0.0
                val kmActual = r.km.toIntOrNull() ?: 0

                totalLitros += litros; totalImporte += importe; litrosAcumulados += litros

                if (r.depositoLleno && i > 0) {
                    val kmRecorridos = kmActual - ultimoKmLleno
                    if (kmRecorridos > 0) consumos.add((litrosAcumulados / kmRecorridos) * 100)
                    litrosAcumulados = 0.0; ultimoKmLleno = kmActual
                }
            }

            fun agregarTarjeta(titulo: String, valor: String, sufijo: String) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40)
                    background = GradientDrawable().apply { setColor(Color.parseColor("#222222")); cornerRadius = 30f }
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 20) }
                }
                val tvTitulo = TextView(this).apply { text = titulo; setTextColor(Color.LTGRAY); textSize = 16f }
                val rowVal = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM }
                val tvValor = TextView(this).apply { text = valor; setTextColor(orangeColor); textSize = 32f; setTypeface(null, Typeface.BOLD) }
                val tvSufijo = TextView(this).apply { text = " $sufijo"; setTextColor(Color.WHITE); textSize = 16f; setPadding(0, 0, 0, 5) }
                rowVal.addView(tvValor); rowVal.addView(tvSufijo); card.addView(tvTitulo); card.addView(rowVal)
                statsLayout.addView(card)
            }

            if (consumos.isNotEmpty()) agregarTarjeta("Consumo Medio", "%.2f".format(consumos.average()), "L/100km")
            else agregarTarjeta("Consumo Medio", "--", "Faltan repostajes 'Llenos'")

            if (totalLitros > 0) agregarTarjeta("Precio Medio", "%.3f".format(totalImporte / totalLitros), "€/L")

            val kmTotales = (repostajes.last().km.toIntOrNull() ?: 0) - (repostajes.first().km.toIntOrNull() ?: 0)
            if (kmTotales > 0) agregarTarjeta("Coste por Km", "%.3f".format(totalImporte / kmTotales), "€/km")
        }

        spinnerStats.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { calcularStats(spinnerStats.selectedItem.toString()) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ==========================================
    // PANTALLA GRÁFICO DE TARTA
    // ==========================================
    fun mostrarPantallaResumen() {
        contentLayout.removeAllViews()
        rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        contentLayout.addView(layout)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply {
            text = "← Volver"; setTextColor(orangeColor); textSize = 18f
            setOnClickListener { mostrarPantallaPrincipal() }
        }
        val title = TextView(this).apply { text = "  Resumen Gráfico"; setTextColor(Color.WHITE); textSize = 24f; setTypeface(null, Typeface.BOLD) }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerResumen = Spinner(this)
        val opcionesResumen = mutableListOf("Todos los coches"); opcionesResumen.addAll(listaCoches.distinct())
        spinnerResumen.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesResumen)
        layout.addView(spinnerResumen)

        val txtTotal = TextView(this).apply { setTextColor(Color.WHITE); textSize = 22f; setTypeface(null, Typeface.BOLD); setPadding(0, 30, 0, 30) }
        layout.addView(txtTotal)

        val pieChart = PieChartView(this)
        layout.addView(pieChart, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500))

        val legendLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 20, 50, 20); gravity = Gravity.CENTER_HORIZONTAL }
        layout.addView(legendLayout)

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

                legendLayout.removeAllViews()
                var colorIdx = 0
                for ((tipo, valor) in datosTarta) {
                    if (valor > 0) {
                        val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 10, 0, 10) }
                        val colorBox = View(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(35, 35)
                            background = GradientDrawable().apply { setColor(Color.parseColor(cardColors[colorIdx % cardColors.size])); cornerRadius = 5f }
                        }
                        val text = TextView(this@MainActivity).apply { text = "  $tipo: %.2f €".format(valor); setTextColor(Color.WHITE); textSize = 16f }
                        row.addView(colorBox); row.addView(text)
                        legendLayout.addView(row)
                        colorIdx++
                    }
                }
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
            val minSize = width.coerceAtMost(height).toFloat()
            val radius = (minSize / 2f) - padding
            val centerX = width / 2f
            val centerY = height / 2f
            rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

            var colorIdx = 0
            for ((_, valor) in data) {
                val sweepAngle = (valor / total) * 360f
                paint.color = colors[colorIdx % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
                colorIdx++
            }
            paint.color = Color.parseColor("#121212")
            canvas.drawCircle(centerX, centerY, radius * 0.5f, paint)
        }
    }

    // ==========================================
    // CSV Y RESULTADOS DE ACTIVIDADES
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
        
        if (requestCode == 300 && resultCode == RESULT_OK && data?.data != null) {
            uriSeleccionada = data.data
            try {
                contentResolver.takePersistableUriPermission(uriSeleccionada!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { }
            txtArchivoGlobal?.text = "✅ Archivo seleccionado"
            return
        }

        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (requestCode == 100 && uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        val csv = StringBuilder("Coche,Tipo,Litros,Km,Importe,Fecha,Lleno,Observaciones,Documento\n")
                        for (g in listaGastos) csv.append("${g.coche},${g.tipo},${g.litros},${g.km},${g.importe},${g.fecha},${g.depositoLleno},\"${g.observaciones}\",${g.documentoUri ?: ""}\n")
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
                            if (p.size >= 6) {
                                listaGastos.add(Gasto(p[0], p[1], p[2], p[3], p[4], p[5], cardColors[colorIndex++ % cardColors.size], p.getOrNull(6)?.toBoolean() ?: false, p.getOrNull(7) ?: "", p.getOrNull(8) ?: null))
                                if (!listaCoches.contains(p[0])) listaCoches.add(p[0])
                            }
                        }
                        guardarDatos()
                        mostrarPantallaPrincipal()
                    }
                    Toast.makeText(this, "¡Importado!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error al importar", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}

data class Gasto(
    val coche: String,
    val tipo: String,
    val litros: String,
    val km: String,
    val importe: String,
    val fecha: String,
    val colorHex: String,
    val depositoLleno: Boolean,
    val observaciones: String,
    val documentoUri: String?
)
