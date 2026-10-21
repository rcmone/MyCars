package com.example.MyCars // ⚠️ Deja tu package original aquí

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    val listaGastos = mutableListOf<Gasto>()
    val listaCoches = mutableListOf("Coche Principal")
    val listaRecordatorios = mutableListOf<Recordatorio>()
    
    // LISTA PARA GUARDAR LOS GASTOS SELECCIONADOS
    val seleccionados = mutableSetOf<Gasto>()
    
    var colorIndex = 0

    val bgColor = Color.parseColor("#121212")
    val orangeColor = Color.parseColor("#FF9800")
    val cardColors = listOf("#E91E63", "#673AB7", "#3F51B5", "#009688", "#FF5722")

    lateinit var rootLayout: FrameLayout
    lateinit var contentLayout: LinearLayout

    var uriSeleccionada: Uri? = null
    var fotoUri: Uri? = null
    var txtArchivoGlobal: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                obj.put("coche", g.coche); obj.put("tipo", g.tipo); obj.put("litros", g.litros)
                obj.put("km", g.km); obj.put("importe", g.importe); obj.put("fecha", g.fecha)
                obj.put("colorHex", g.colorHex); obj.put("depositoLleno", g.depositoLleno)
                obj.put("observaciones", g.observaciones); obj.put("documentoUri", g.documentoUri ?: "")
                jsonArray.put(obj)
            }
            openFileOutput("gastos.json", MODE_PRIVATE).use { stream -> stream.write(jsonArray.toString().toByteArray()) }
            
            val cochesArray = JSONArray()
            for (c in listaCoches) cochesArray.put(c)
            openFileOutput("coches.json", MODE_PRIVATE).use { stream -> stream.write(cochesArray.toString().toByteArray()) }

            val recArray = JSONArray()
            for (r in listaRecordatorios) {
                val obj = JSONObject(); obj.put("titulo", r.titulo); obj.put("fecha", r.fecha); obj.put("coche", r.coche)
                recArray.put(obj)
            }
            openFileOutput("recordatorios.json", MODE_PRIVATE).use { stream -> stream.write(recArray.toString().toByteArray()) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun cargarDatos() {
        try {
            val jsonString = openFileInput("coches.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            listaCoches.clear()
            for (i in 0 until jsonArray.length()) { listaCoches.add(jsonArray.getString(i)) }
        } catch (e: java.io.FileNotFoundException) {} catch (e: Exception) { e.printStackTrace() }

        try {
            val jsonString = openFileInput("gastos.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            listaGastos.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                listaGastos.add(Gasto(obj.getString("coche"), obj.getString("tipo"), obj.optString("litros", ""), obj.optString("km", ""), obj.getString("importe"), obj.getString("fecha"), obj.getString("colorHex"), obj.optBoolean("depositoLleno", false), obj.optString("observaciones", ""), obj.optString("documentoUri").takeIf { it.isNotEmpty() }))
            }
            colorIndex = listaGastos.size
        } catch (e: java.io.FileNotFoundException) {} catch (e: Exception) { e.printStackTrace() }

        try {
            val jsonString = openFileInput("recordatorios.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            listaRecordatorios.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                listaRecordatorios.add(Recordatorio(obj.getString("titulo"), obj.getString("fecha"), obj.getString("coche")))
            }
        } catch (e: java.io.FileNotFoundException) {} catch (e: Exception) { e.printStackTrace() }
    }

    // ==========================================
    // PANTALLA PRINCIPAL
    // ==========================================
    fun mostrarPantallaPrincipal() {
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val header = RelativeLayout(this).apply { setPadding(50, 80, 50, 20) }
        
        val title = TextView(this).apply {
            text = "My Cars"; setTextColor(orangeColor); textSize = 32f; setTypeface(null, Typeface.BOLD)
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT); params.addRule(RelativeLayout.CENTER_VERTICAL); layoutParams = params
        }
        header.addView(title)

        val iconsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); layoutParams = params
        }

        val iconCalendar = TextView(this).apply { text = "📅"; textSize = 24f; setPadding(0, 0, 30, 0); setOnClickListener { mostrarPantallaRecordatorios() } }
        val iconLineChart = TextView(this).apply { text = "📉"; textSize = 24f; setPadding(0, 0, 30, 0); setOnClickListener { mostrarPantallaEvolucion() } }
        val iconStats = TextView(this).apply { text = "📈"; textSize = 24f; setPadding(0, 0, 30, 0); setOnClickListener { mostrarPantallaEstadisticas() } }
        val iconChart = TextView(this).apply { text = "📊"; textSize = 24f; setPadding(0, 0, 30, 0); setOnClickListener { mostrarPantallaResumen() } }
        val iconMenu = TextView(this).apply { text = "⋮"; setTextColor(Color.WHITE); textSize = 28f; setPadding(0, 0, 0, 15); setOnClickListener { v -> showMenu(v) } }

        iconsLayout.addView(iconCalendar); iconsLayout.addView(iconLineChart); iconsLayout.addView(iconStats); iconsLayout.addView(iconChart); iconsLayout.addView(iconMenu)
        header.addView(iconsLayout); contentLayout.addView(header)

        val searchBar = EditText(this).apply {
            hint = "🔍  Buscar..."; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); textSize = 16f; setPadding(40, 25, 40, 25)
            background = GradientDrawable().apply { setColor(Color.parseColor("#222222")); cornerRadius = 40f }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(50, 0, 50, 30); layoutParams = lp
        }
        contentLayout.addView(searchBar)

        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 0, 50, 150) }
        scrollView.addView(listLayout); contentLayout.addView(scrollView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        fun renderLista(filtro: String = "") {
            listLayout.removeAllViews()
            val filtrados = listaGastos.filter { it.coche.contains(filtro, true) || it.tipo.contains(filtro, true) }
            
            for (g in filtrados.reversed()) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40)
                    background = GradientDrawable().apply { setColor(Color.parseColor(g.colorHex)); cornerRadius = 40f }
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.setMargins(0, 0, 0, 30); layoutParams = lp
                }

                val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                
                // --- CHECKBOX PARA SELECCIONAR ---
                val checkBox = CheckBox(this).apply {
                    isChecked = seleccionados.contains(g)
                    buttonTintList = ColorStateList.valueOf(Color.WHITE)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) seleccionados.add(g) else seleccionados.remove(g)
                    }
                }
                
                val txtTitulo = TextView(this).apply {
                    text = "${g.coche} - ${g.tipo}"; setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val txtImporte = TextView(this).apply { text = "${g.importe} €"; setTextColor(Color.WHITE); textSize = 20f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 20, 0) }
                
                val btnDelete = TextView(this).apply {
                    text = "🗑️"; textSize = 20f
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity).setTitle("Eliminar").setMessage("¿Borrar este gasto?")
                            .setPositiveButton("Sí") { _, _ -> 
                                listaGastos.remove(g)
                                seleccionados.remove(g) // Lo quitamos también de seleccionados
                                guardarDatos(); renderLista(searchBar.text.toString()) 
                            }
                            .setNegativeButton("No", null).show()
                    }
                }

                topRow.addView(checkBox) // Añadimos el checkbox al principio
                topRow.addView(txtTitulo); topRow.addView(txtImporte); topRow.addView(btnDelete)

                val botRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 10, 0, 0) }
                val txtSubtitulo = TextView(this).apply {
                    var sub = ""
                    if (g.litros.isNotEmpty()) sub += "${g.litros} L/kWh  "
                    if (g.km.isNotEmpty()) sub += "${g.km} Km"
                    if (g.depositoLleno) sub += " (Lleno)"
                    text = sub; setTextColor(Color.WHITE); textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val txtFecha = TextView(this).apply { text = g.fecha; setTextColor(Color.WHITE); textSize = 14f }
                botRow.addView(txtSubtitulo); botRow.addView(txtFecha)

                card.addView(topRow); card.addView(botRow)

                if (g.observaciones.isNotEmpty()) {
                    val txtObs = TextView(this).apply { text = "📝 ${g.observaciones}"; setTextColor(Color.WHITE); textSize = 14f; setPadding(0, 10, 0, 0) }
                    card.addView(txtObs)
                }

                if (g.documentoUri != null) {
                    val txtDoc = TextView(this).apply {
                        text = "📎 Ver documento adjunto"; setTextColor(Color.parseColor("#FFD54F")); textSize = 14f; setPadding(0, 10, 0, 0)
                        setOnClickListener {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(g.documentoUri))
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(intent)
                            } catch (e: Exception) { Toast.makeText(this@MainActivity, "No se puede abrir", Toast.LENGTH_SHORT).show() }
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
            text = "＋ Añadir"; setTextColor(Color.BLACK); textSize = 16f; setTypeface(null, Typeface.BOLD)
            background = GradientDrawable().apply { setColor(orangeColor); cornerRadius = 50f }
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM or Gravity.END; params.setMargins(0, 0, 50, 50)
            layoutParams = params; setOnClickListener { mostrarFormularioNuevoGasto() }
        }
        rootLayout.addView(fab)
    }

    fun showMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Exportar a CSV")
        popup.menu.add("Importar desde CSV")
        popup.menu.add("☁️ Copia de Seguridad (JSON)")
        popup.menu.add("☁️ Restaurar Copia (JSON)")
        popup.menu.add("📄 Generar PDF (Seleccionados)")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Exportar a CSV" -> { exportarCSV(); true }
                "Importar desde CSV" -> { importarCSV(); true }
                "☁️ Copia de Seguridad (JSON)" -> { exportarJSON(); true }
                "☁️ Restaurar Copia (JSON)" -> { importarJSON(); true }
                "📄 Generar PDF (Seleccionados)" -> { generarPDF(); true }
                else -> false
            }
        }
        popup.show()
    }

    // ==========================================
    // GENERAR PDF
    // ==========================================
    fun generarPDF() {
        if (seleccionados.isEmpty()) {
            Toast.makeText(this, "Primero marca la casilla de los gastos que quieras incluir", Toast.LENGTH_LONG).show()
            return
        }
        
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Reporte_Gastos_MyCars.pdf")
        }
        startActivityForResult(intent, 700)
    }

    // ==========================================
    // FORMULARIO NUEVO GASTO
    // ==========================================
    fun mostrarFormularioNuevoGasto() {
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        uriSeleccionada = null

        val scroll = ScrollView(this); val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        scroll.addView(layout); contentLayout.addView(scroll)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply { text = "← Volver"; setTextColor(orangeColor); textSize = 18f; setOnClickListener { mostrarPantallaPrincipal() } }
        val title = TextView(this).apply { text = "  Nuevo Gasto"; setTextColor(Color.WHITE); textSize = 24f; setTypeface(null, Typeface.BOLD) }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerCoche = Spinner(this)
        spinnerCoche.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCoches)
        layout.addView(spinnerCoche)

        val btnEditCoches = Button(this).apply {
            text = "✏️ Editar nombres de coches"; setBackgroundColor(Color.TRANSPARENT); setTextColor(orangeColor)
            setOnClickListener {
                val dialogLayout = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 30, 50, 30) }
                val editTexts = mutableListOf<EditText>()
                fun agregarCampo(nombre: String) {
                    val et = EditText(this@MainActivity).apply { setText(nombre); setTextColor(Color.WHITE); hint = "Nombre" }
                    editTexts.add(et); dialogLayout.addView(et)
                }
                listaCoches.forEach { agregarCampo(it) }
                dialogLayout.addView(Button(this@MainActivity).apply { text = "+ Añadir otro"; setOnClickListener { agregarCampo("") } })

                AlertDialog.Builder(this@MainActivity).setTitle("Coches").setView(ScrollView(this@MainActivity).apply { addView(dialogLayout) })
                    .setPositiveButton("Guardar") { _, _ ->
                        listaCoches.clear()
                        for (et in editTexts) { val n = et.text.toString().trim(); if (n.isNotEmpty() && !listaCoches.contains(n)) listaCoches.add(n) }
                        if (listaCoches.isEmpty()) listaCoches.add("Coche Principal")
                        spinnerCoche.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listaCoches)
                        guardarDatos()
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        layout.addView(btnEditCoches)

        val spinnerGasto = Spinner(this)
        val tipos = arrayOf("Combustible", "Mantenimiento", "Otros")
        spinnerGasto.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        layout.addView(spinnerGasto)

        val inputImporte = EditText(this).apply { hint = "Importe total (€)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
        layout.addView(inputImporte)

        val inputLitros = EditText(this).apply { hint = "Cantidad (Litros o kWh)"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setTextColor(Color.WHITE) }
        val inputKm = EditText(this).apply { hint = "Kilometraje actual (Km)"; inputType = InputType.TYPE_CLASS_NUMBER; setTextColor(Color.WHITE) }
        val checkLleno = CheckBox(this).apply { text = "Depósito lleno"; setTextColor(Color.WHITE); buttonTintList = ColorStateList.valueOf(orangeColor) }
        
        val txtPrecioLitro = TextView(this).apply { setTextColor(orangeColor); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 10, 0, 20) }

        layout.addView(inputLitros); layout.addView(checkLleno); layout.addView(inputKm); layout.addView(txtPrecioLitro)

        val layoutExtras = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        val inputObservaciones = EditText(this).apply { hint = "Observaciones"; setTextColor(Color.WHITE) }
        
        val btnCamara = Button(this).apply {
            text = "📷 Hacer foto a la factura"; setBackgroundColor(Color.parseColor("#333333")); setTextColor(Color.WHITE)
            setOnClickListener {
                if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
                    return@setOnClickListener
                }
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val ficheroFoto = File(getExternalFilesDir(null), "factura_${System.currentTimeMillis()}.jpg")
                try {
                    fotoUri = androidx.core.content.FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", ficheroFoto)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
                    startActivityForResult(cameraIntent, 400)
                } catch (e: Exception) { 
                    AlertDialog.Builder(this@MainActivity).setTitle("Error cámara").setMessage("Detalle: ${e.message}").setPositiveButton("OK", null).show()
                }
            }
        }

        val btnAdjuntar = Button(this).apply {
            text = "📎 Elegir de la galería"; setBackgroundColor(Color.parseColor("#333333")); setTextColor(Color.WHITE)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, 300)
            }
        }
        
        val txtArchivoSeleccionado = TextView(this).apply { text = "Ningún archivo seleccionado"; setTextColor(Color.LTGRAY); setPadding(0, 10, 0, 10) }

        layoutExtras.addView(inputObservaciones); layoutExtras.addView(btnCamara); layoutExtras.addView(btnAdjuntar); layoutExtras.addView(txtArchivoSeleccionado)
        layout.addView(layoutExtras)

        this.txtArchivoGlobal = txtArchivoSeleccionado

        val watcherCalculadora = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val imp = inputImporte.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                val lit = inputLitros.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
                if (lit > 0.0 && imp > 0.0) txtPrecioLitro.text = "👉 Precio: %.3f €/L".format(imp / lit) else txtPrecioLitro.text = ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        inputImporte.addTextChangedListener(watcherCalculadora); inputLitros.addTextChangedListener(watcherCalculadora)

        spinnerGasto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (tipos[position] == "Combustible") {
                    inputLitros.visibility = View.VISIBLE; checkLleno.visibility = View.VISIBLE
                    inputKm.visibility = View.VISIBLE; txtPrecioLitro.visibility = View.VISIBLE; layoutExtras.visibility = View.GONE
                } else {
                    inputLitros.visibility = View.GONE; checkLleno.visibility = View.GONE
                    inputKm.visibility = View.VISIBLE; txtPrecioLitro.visibility = View.GONE; layoutExtras.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val btnGuardar = Button(this).apply {
            text = "Guardar Gasto"; setBackgroundColor(orangeColor); setTextColor(Color.BLACK); setTypeface(null, Typeface.BOLD); setPadding(0, 30, 0, 30)
            setOnClickListener {
                val coche = spinnerCoche.selectedItem.toString()
                val importe = inputImporte.text.toString().replace(",", ".")
                if (importe.isNotEmpty()) {
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    listaGastos.add(Gasto(coche, spinnerGasto.selectedItem.toString(), inputLitros.text.toString().replace(",", "."), inputKm.text.toString(), importe, fecha, cardColors[colorIndex % cardColors.size], checkLleno.isChecked, inputObservaciones.text.toString(), uriSeleccionada?.toString()))
                    colorIndex++; guardarDatos(); Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show(); mostrarPantallaPrincipal()
                } else Toast.makeText(this@MainActivity, "El importe es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(btnGuardar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 40, 0, 0) })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Pulsa de nuevo para hacer la foto.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permiso denegado. Ve a los ajustes.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==========================================
    // PANTALLA RECORDATORIOS
    // ==========================================
    fun mostrarPantallaRecordatorios() {
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        scroll.addView(layout); contentLayout.addView(scroll)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply { text = "← Volver"; setTextColor(orangeColor); textSize = 18f; setOnClickListener { mostrarPantallaPrincipal() } }
        val title = TextView(this).apply { text = "  Recordatorios"; setTextColor(Color.WHITE); textSize = 24f; setTypeface(null, Typeface.BOLD) }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val btnAddRec = Button(this).apply {
            text = "＋ Añadir Recordatorio (ITV, Seguro...)"
            setBackgroundColor(orangeColor); setTextColor(Color.BLACK); setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                val dialogLayout = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 30, 50, 30) }
                val etTitulo = EditText(this@MainActivity).apply { hint = "Ej. Renovar ITV"; setTextColor(Color.BLACK) }
                val etCoche = Spinner(this@MainActivity).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listaCoches) }
                val btnFecha = Button(this@MainActivity).apply { text = "Elegir fecha"; setBackgroundColor(Color.LTGRAY); setTextColor(Color.BLACK) }
                val tvFecha = TextView(this@MainActivity).apply { text = "Fecha: Sin elegir"; setTextColor(Color.BLACK); setPadding(0, 20, 0, 20) }
                var fechaElegida = ""

                btnFecha.setOnClickListener {
                    val c = Calendar.getInstance()
                    DatePickerDialog(this@MainActivity, { _, year, month, day ->
                        fechaElegida = "%02d/%02d/%d".format(day, month + 1, year)
                        tvFecha.text = "Fecha: $fechaElegida"
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                }

                dialogLayout.addView(etTitulo); dialogLayout.addView(etCoche); dialogLayout.addView(btnFecha); dialogLayout.addView(tvFecha)

                AlertDialog.Builder(this@MainActivity).setTitle("Nuevo Recordatorio").setView(dialogLayout)
                    .setPositiveButton("Guardar") { _, _ ->
                        if (etTitulo.text.isNotEmpty() && fechaElegida.isNotEmpty()) {
                            listaRecordatorios.add(Recordatorio(etTitulo.text.toString(), fechaElegida, etCoche.selectedItem.toString()))
                            guardarDatos(); mostrarPantallaRecordatorios()
                        }
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        layout.addView(btnAddRec, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 30) })

        for (r in listaRecordatorios) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; setPadding(40, 40, 40, 40)
                background = GradientDrawable().apply { setColor(Color.parseColor("#222222")); cornerRadius = 30f }
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 20) }
            }
            val tvInfo = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            tvInfo.addView(TextView(this).apply { text = r.titulo; setTextColor(orangeColor); textSize = 18f; setTypeface(null, Typeface.BOLD) })
            tvInfo.addView(TextView(this).apply { text = "${r.coche} | ${r.fecha}"; setTextColor(Color.WHITE); textSize = 14f })
            
            val btnDel = TextView(this).apply { 
                text = "🗑️"; textSize = 20f; gravity = Gravity.CENTER_VERTICAL
                setOnClickListener { listaRecordatorios.remove(r); guardarDatos(); mostrarPantallaRecordatorios() }
            }
            card.addView(tvInfo); card.addView(btnDel)
            layout.addView(card)
        }
    }

    // ==========================================
    // PANTALLA EVOLUCIÓN DEL PRECIO
    // ==========================================
    fun mostrarPantallaEvolucion() {
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        contentLayout.addView(layout)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply { text = "← Volver"; setTextColor(orangeColor); textSize = 18f; setOnClickListener { mostrarPantallaPrincipal() } }
        val title = TextView(this).apply { text = "  Evolución Precio"; setTextColor(Color.WHITE); textSize = 24f; setTypeface(null, Typeface.BOLD) }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerCoche = Spinner(this)
        val opciones = mutableListOf("Todos los coches"); opciones.addAll(listaCoches.distinct())
        spinnerCoche.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        layout.addView(spinnerCoche)

        val lineChart = LineChartView(this)
        layout.addView(lineChart, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800))

        fun actualizarGrafico(cocheSeleccionado: String) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val repostajes = listaGastos.filter { 
                (cocheSeleccionado == "Todos los coches" || it.coche == cocheSeleccionado) && it.tipo == "Combustible" && it.litros.isNotEmpty() && it.importe.isNotEmpty() 
            }.sortedBy { try { formatter.parse(it.fecha)?.time ?: 0L } catch(e: Exception) { 0L } }

            val datosGrafico = mutableListOf<Pair<String, Float>>()
            for (r in repostajes) {
                val litros = r.litros.replace(",", ".").toFloatOrNull() ?: 1f
                val importe = r.importe.replace(",", ".").toFloatOrNull() ?: 0f
                if (litros > 0f) datosGrafico.add(Pair(r.fecha.substring(0, 5), importe / litros))
            }
            lineChart.setData(datosGrafico)
        }

        spinnerCoche.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { actualizarGrafico(spinnerCoche.selectedItem.toString()) }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    class LineChartView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var data: List<Pair<String, Float>> = emptyList()
        private val lineColor = Color.parseColor("#FF9800"); private val textColor = Color.WHITE; private val dotColor = Color.parseColor("#E91E63")

        fun setData(newData: List<Pair<String, Float>>) { data = newData; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (data.isEmpty()) {
                paint.color = Color.GRAY; paint.textSize = 40f; paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Sin datos", width / 2f, height / 2f, paint); return
            }
            if (data.size == 1) {
                paint.color = dotColor; canvas.drawCircle(width / 2f, height / 2f, 15f, paint)
                paint.color = textColor; paint.textSize = 30f; paint.textAlign = Paint.Align.CENTER
                canvas.drawText("%.2f €".format(data[0].second), width / 2f, height / 2f - 40f, paint); return
            }

            val paddingLeft = 80f; val paddingRight = 50f; val paddingTop = 80f; val paddingBottom = 100f
            val chartWidth = width - paddingLeft - paddingRight; val chartHeight = height - paddingTop - paddingBottom
            val prices = data.map { it.second }; val maxPrice = prices.maxOrNull() ?: 0f; val minPrice = prices.minOrNull() ?: 0f
            val range = if (maxPrice == minPrice) 1f else maxPrice - minPrice

            paint.color = Color.parseColor("#333333"); paint.strokeWidth = 3f
            canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paint)

            val points = mutableListOf<PointF>(); val stepX = chartWidth / (data.size - 1)
            for (i in data.indices) {
                val x = paddingLeft + (i * stepX)
                val y = (height - paddingBottom) - (((data[i].second - minPrice) / range) * chartHeight)
                points.add(PointF(x, y))
            }

            paint.color = lineColor; paint.strokeWidth = 6f; paint.style = Paint.Style.STROKE
            val path = Path(); path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) path.lineTo(points[i].x, points[i].y)
            canvas.drawPath(path, paint)

            paint.style = Paint.Style.FILL; paint.textAlign = Paint.Align.CENTER; paint.textSize = 26f
            for (i in points.indices) {
                val pt = points[i]
                paint.color = dotColor; canvas.drawCircle(pt.x, pt.y, 12f, paint)
                paint.color = textColor; canvas.drawText("%.2f".format(data[i].second), pt.x, pt.y - 30f, paint)
                paint.color = Color.LTGRAY; paint.textSize = 22f; canvas.drawText(data[i].first, pt.x, height - paddingBottom + 40f, paint)
                paint.textSize = 26f
            }
        }
    }

    // ==========================================
    // PANTALLA ESTADÍSTICAS
    // ==========================================
    fun mostrarPantallaEstadisticas() {
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val scroll = ScrollView(this); val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        scroll.addView(layout); contentLayout.addView(scroll)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply { text = "← Volver"; setTextColor(orangeColor); textSize = 18f; setOnClickListener { mostrarPantallaPrincipal() } }
        val title = TextView(this).apply { text = "  Estadísticas y Costes"; setTextColor(Color.WHITE); textSize = 24f; setTypeface(null, Typeface.BOLD) }
        header.addView(btnBack); header.addView(title)
        layout.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,40) })

        val spinnerStats = Spinner(this)
        val opciones = mutableListOf("Todos los coches"); opciones.addAll(listaCoches.distinct())
        spinnerStats.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        layout.addView(spinnerStats)

        val statsLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 30, 0, 0) }
        layout.addView(statsLayout)

        fun calcularStats(cocheSeleccionado: String) {
            statsLayout.removeAllViews()
            val repostajes = listaGastos.filter { 
                (cocheSeleccionado == "Todos los coches" || it.coche == cocheSeleccionado) && it.tipo == "Combustible" && it.litros.isNotEmpty() && it.km.isNotEmpty() 
            }.sortedBy { it.km.toIntOrNull() ?: 0 }

            if (repostajes.size < 2) {
                statsLayout.addView(TextView(this).apply { text = "Necesitas al menos 2 repostajes con Km."; setTextColor(Color.LTGRAY) })
            } else {
                var totalLitros = 0.0; var totalImporte = 0.0
                var consumos = mutableListOf<Double>(); var litrosAcumulados = 0.0; var ultimoKmLleno = repostajes.first().km.toIntOrNull() ?: 0

                for (i in repostajes.indices) {
                    val r = repostajes[i]
                    val litros = r.litros.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val importe = r.importe.replace(",", ".").toDoubleOrNull() ?: 0.0
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
                    rowVal.addView(tvValor); rowVal.addView(tvSufijo); card.addView(tvTitulo); card.addView(rowVal); statsLayout.addView(card)
                }

                if (consumos.isNotEmpty()) agregarTarjeta("Consumo Medio", "%.2f".format(consumos.average()), "L/100km")
                else agregarTarjeta("Consumo Medio", "--", "Faltan repostajes 'Llenos'")
                if (totalLitros > 0) agregarTarjeta("Precio Medio Combustible", "%.3f".format(totalImporte / totalLitros), "€/L")
                val kmTotalesCombustible = (repostajes.last().km.toIntOrNull() ?: 0) - (repostajes.first().km.toIntOrNull() ?: 0)
                if (kmTotalesCombustible > 0) agregarTarjeta("Coste solo Gasolina/Km", "%.3f".format(totalImporte / kmTotalesCombustible), "€/km")
            }

            val gastosCoche = listaGastos.filter { (cocheSeleccionado == "Todos los coches" || it.coche == cocheSeleccionado) }
            val gastosConKm = gastosCoche.filter { it.km.isNotEmpty() && it.km.toIntOrNull() != null }

            if (gastosConKm.size >= 2) {
                val listaKms = gastosConKm.map { it.km.toInt() }
                val kmMin = listaKms.minOrNull() ?: 0; val kmMax = listaKms.maxOrNull() ?: 0
                val kmTotalesReales = kmMax - kmMin
                if (kmTotalesReales > 0) {
                    var gastoTotalReal = 0.0
                    for (g in gastosCoche) gastoTotalReal += g.importe.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val costeRealKm = gastoTotalReal / kmTotalesReales

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40)
                        background = GradientDrawable().apply { setColor(Color.parseColor("#2A1A10")); setStroke(4, orangeColor); cornerRadius = 30f }
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 10, 0, 20) }
                    }
                    val tvTitulo = TextView(this).apply { text = "Coste REAL por Km (Todo)"; setTextColor(orangeColor); textSize = 16f; setTypeface(null, Typeface.BOLD) }
                    val rowVal = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.BOTTOM }
                    val tvValor = TextView(this).apply { text = "%.3f".format(costeRealKm); setTextColor(Color.WHITE); textSize = 36f; setTypeface(null, Typeface.BOLD) }
                    val tvSufijo = TextView(this).apply { text = " €/km"; setTextColor(Color.WHITE); textSize = 16f; setPadding(0, 0, 0, 5) }
                    val tvDetalle = TextView(this).apply { 
                        text = "Calculado sobre $kmTotalesReales km.\nGasto total: %.2f €".format(gastoTotalReal); setTextColor(Color.LTGRAY); textSize = 12f; setPadding(0, 10, 0, 0)
                    }
                    rowVal.addView(tvValor); rowVal.addView(tvSufijo); card.addView(tvTitulo); card.addView(rowVal); card.addView(tvDetalle)
                    statsLayout.addView(card)
                }
            } else {
                statsLayout.addView(TextView(this).apply { text = "\nAñade más gastos con Km para el Coste Real."; setTextColor(Color.LTGRAY) })
            }
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
        contentLayout.removeAllViews(); rootLayout.removeAllViews()
        rootLayout.addView(contentLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 80, 50, 50) }
        contentLayout.addView(layout)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val btnBack = TextView(this).apply { text = "← Volver"; setTextColor(orangeColor); textSize = 18f; setOnClickListener { mostrarPantallaPrincipal() } }
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
                var total = 0.0; val datosTarta = mutableMapOf<String, Float>()
                for (g in filtrados) {
                    val imp = g.importe.replace(",", ".").toDoubleOrNull() ?: 0.0
                    total += imp; datosTarta[g.tipo] = (datosTarta[g.tipo] ?: 0f) + imp.toFloat()
                }
                txtTotal.text = "Gasto Total: %.2f €".format(total); pieChart.setData(datosTarta)

                legendLayout.removeAllViews(); var colorIdx = 0
                for ((tipo, valor) in datosTarta) {
                    if (valor > 0) {
                        val row = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 10, 0, 10) }
                        val colorBox = View(this@MainActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(35, 35)
                            background = GradientDrawable().apply { setColor(Color.parseColor(cardColors[colorIdx % cardColors.size])); cornerRadius = 5f }
                        }
                        val text = TextView(this@MainActivity).apply { text = "  $tipo: %.2f €".format(valor); setTextColor(Color.WHITE); textSize = 16f }
                        row.addView(colorBox); row.addView(text); legendLayout.addView(row); colorIdx++
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    class PieChartView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG); private val rectF = RectF()
        private var data: Map<String, Float> = emptyMap()
        private val colors = listOf(Color.parseColor("#E91E63"), Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"), Color.parseColor("#009688"), Color.parseColor("#FF5722"))

        fun setData(newData: Map<String, Float>) { data = newData; invalidate() }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (data.isEmpty()) return
            val total = data.values.sum(); var startAngle = -90f; val padding = 20f
            val minSize = width.coerceAtMost(height).toFloat(); val radius = (minSize / 2f) - padding
            val centerX = width / 2f; val centerY = height / 2f
            rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

            var colorIdx = 0
            for ((_, valor) in data) {
                val sweepAngle = (valor / total) * 360f
                paint.color = colors[colorIdx % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle; colorIdx++
            }
            paint.color = Color.parseColor("#121212"); canvas.drawCircle(centerX, centerY, radius * 0.5f, paint)
        }
    }

    // ==========================================
    // COPIA DE SEGURIDAD JSON (NUBE)
    // ==========================================
    fun exportarJSON() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "application/json"; putExtra(Intent.EXTRA_TITLE, "backup_mycars.json")
        }
        startActivityForResult(intent, 500)
    }

    fun importarJSON() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
        }
        startActivityForResult(intent, 600)
    }

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
        
        if (requestCode == 400 && resultCode == RESULT_OK && fotoUri != null) {
            uriSeleccionada = fotoUri; txtArchivoGlobal?.text = "✅ Foto capturada"; return
        }
        if (requestCode == 300 && resultCode == RESULT_OK && data?.data != null) {
            uriSeleccionada = data.data
            try { contentResolver.takePersistableUriPermission(uriSeleccionada!!, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            txtArchivoGlobal?.text = "✅ Archivo seleccionado"; return
        }

        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            
            if (requestCode == 700 && uri != null) {
                // GENERAR EL PDF
                try {
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Tamaño A4
                    var page = pdfDocument.startPage(pageInfo)
                    var canvas = page.canvas
                    
                    val paint = Paint()
                    var y = 50f
                    
                    // Título
                    paint.textSize = 22f
                    paint.isFakeBoldText = true
                    canvas.drawText("Reporte de Gastos - MyCars", 40f, y, paint)
                    y += 30f
                    
                    paint.textSize = 12f
                    paint.isFakeBoldText = false
                    
                    // Cabecera de la tabla
                    canvas.drawText("Fecha", 40f, y, paint)
                    canvas.drawText("Coche", 120f, y, paint)
                    canvas.drawText("Tipo", 220f, y, paint)
                    canvas.drawText("Importe", 350f, y, paint)
                    canvas.drawText("Km", 450f, y, paint)
                    y += 15f
                    canvas.drawLine(40f, y, 550f, y, paint)
                    y += 20f
                    
                    var totalImporte = 0.0
                    
                    for (gasto in seleccionados) {
                        if (y > 800f) {
                            pdfDocument.finishPage(page)
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            y = 50f
                        }
                        
                        canvas.drawText(gasto.fecha, 40f, y, paint)
                        canvas.drawText(gasto.coche, 120f, y, paint)
                        canvas.drawText(gasto.tipo, 220f, y, paint)
                        canvas.drawText("${gasto.importe} €", 350f, y, paint)
                        canvas.drawText(gasto.km, 450f, y, paint)
                        y += 20f
                        
                        if (gasto.observaciones.isNotEmpty()) {
                            paint.color = Color.GRAY
                            canvas.drawText("  Obs: ${gasto.observaciones}", 120f, y, paint)
                            paint.color = Color.BLACK
                            y += 20f
                        }
                        
                        totalImporte += gasto.importe.replace(",", ".").toDoubleOrNull() ?: 0.0
                        y += 5f // Espacio entre items
                    }
                    
                    // Total final
                    y += 10f
                    paint.isFakeBoldText = true
                    canvas.drawLine(40f, y - 5f, 550f, y - 5f, paint)
                    canvas.drawText("TOTAL:", 350f, y + 10f, paint)
                    canvas.drawText("%.2f €".format(totalImporte), 450f, y + 10f, paint)
                    
                    pdfDocument.finishPage(page)
                    
                    contentResolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    Toast.makeText(this, "¡PDF generado con éxito!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
                return
            }

            if (requestCode == 100 && uri != null) { // CSV EXPORT
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        val csv = StringBuilder("Coche,Tipo,Litros,Km,Importe,Fecha,Lleno,Observaciones,Documento\n")
                        for (g in listaGastos) csv.append("${g.coche},${g.tipo},${g.litros},${g.km},${g.importe},${g.fecha},${g.depositoLleno},\"${g.observaciones}\",${g.documentoUri ?: ""}\n")
                        out.write(csv.toString().toByteArray())
                    }
                    Toast.makeText(this, "¡Exportado CSV!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show() }
            } else if (requestCode == 200 && uri != null) { // CSV IMPORT
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val lines = input.bufferedReader().readLines(); listaGastos.clear()
                        for (i in 1 until lines.size) {
                            val p = lines[i].split(",")
                            if (p.size >= 6) {
                                listaGastos.add(Gasto(p[0], p[1], p[2], p[3], p[4], p[5], cardColors[colorIndex++ % cardColors.size], p.getOrNull(6)?.toBoolean() ?: false, p.getOrNull(7) ?: "", p.getOrNull(8) ?: null))
                                if (!listaCoches.contains(p[0])) listaCoches.add(p[0])
                            }
                        }
                        guardarDatos(); mostrarPantallaPrincipal()
                    }
                    Toast.makeText(this, "¡Importado CSV!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error al importar", Toast.LENGTH_SHORT).show() }
            } else if (requestCode == 500 && uri != null) { // JSON EXPORT
                try {
                    val backupObj = JSONObject()
                    val gastosArray = JSONArray()
                    for (g in listaGastos) {
                        val obj = JSONObject()
                        obj.put("coche", g.coche); obj.put("tipo", g.tipo); obj.put("litros", g.litros)
                        obj.put("km", g.km); obj.put("importe", g.importe); obj.put("fecha", g.fecha)
                        obj.put("colorHex", g.colorHex); obj.put("depositoLleno", g.depositoLleno)
                        obj.put("observaciones", g.observaciones); obj.put("documentoUri", g.documentoUri ?: "")
                        gastosArray.put(obj)
                    }
                    val cochesArray = JSONArray(); for (c in listaCoches) cochesArray.put(c)
                    val recArray = JSONArray()
                    for (r in listaRecordatorios) {
                        val obj = JSONObject(); obj.put("titulo", r.titulo); obj.put("fecha", r.fecha); obj.put("coche", r.coche); recArray.put(obj)
                    }
                    backupObj.put("gastos", gastosArray); backupObj.put("coches", cochesArray); backupObj.put("recordatorios", recArray)
                    
                    contentResolver.openOutputStream(uri)?.use { out -> out.write(backupObj.toString().toByteArray()) }
                    Toast.makeText(this, "¡Copia de seguridad guardada!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error al guardar copia", Toast.LENGTH_SHORT).show() }
            } else if (requestCode == 600 && uri != null) { // JSON IMPORT
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val jsonString = input.bufferedReader().readText()
                        val backupObj = JSONObject(jsonString)
                        
                        listaGastos.clear(); listaCoches.clear(); listaRecordatorios.clear()
                        
                        val gastosArray = backupObj.getJSONArray("gastos")
                        for (i in 0 until gastosArray.length()) {
                            val obj = gastosArray.getJSONObject(i)
                            listaGastos.add(Gasto(obj.getString("coche"), obj.getString("tipo"), obj.optString("litros"), obj.optString("km"), obj.getString("importe"), obj.getString("fecha"), obj.getString("colorHex"), obj.optBoolean("depositoLleno"), obj.optString("observaciones"), obj.optString("documentoUri").takeIf { it.isNotEmpty() }))
                        }
                        val cochesArray = backupObj.getJSONArray("coches")
                        for (i in 0 until cochesArray.length()) listaCoches.add(cochesArray.getString(i))
                        
                        if (backupObj.has("recordatorios")) {
                            val recArray = backupObj.getJSONArray("recordatorios")
                            for (i in 0 until recArray.length()) {
                                val obj = recArray.getJSONObject(i)
                                listaRecordatorios.add(Recordatorio(obj.getString("titulo"), obj.getString("fecha"), obj.getString("coche")))
                            }
                        }
                        colorIndex = listaGastos.size
                        guardarDatos(); mostrarPantallaPrincipal()
                    }
                    Toast.makeText(this, "¡Copia restaurada!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Error al restaurar", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}

data class Gasto(
    val coche: String, val tipo: String, val litros: String, val km: String,
    val importe: String, val fecha: String, val colorHex: String,
    val depositoLleno: Boolean, val observaciones: String, val documentoUri: String?
)

data class Recordatorio(
    val titulo: String, val fecha: String, val coche: String
)
