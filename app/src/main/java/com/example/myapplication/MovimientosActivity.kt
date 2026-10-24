package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovimientosActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var spProducto: Spinner
    lateinit var rbEntrada: RadioButton
    lateinit var etCantidad: EditText
    lateinit var etFecha: EditText
    lateinit var listaMovimientos: ListView
    var productos = ArrayList<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movimientos)

        db = DBHelper(this)
        spProducto = findViewById(R.id.spProducto)
        rbEntrada = findViewById(R.id.rbEntrada)
        etCantidad = findViewById(R.id.etCantidad)
        etFecha = findViewById(R.id.etFechaMovimiento)
        listaMovimientos = findViewById(R.id.listaMovimientos)

        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etFecha.setText(fechaHoy)

        findViewById<Button>(R.id.btnAtras).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnRegistrar).setOnClickListener { registrar() }

        actualizarPantalla()
    }

    private fun registrar() {
        if (productos.isEmpty()) {
            mostrar("Primero agrega productos")
            return
        }

        val cantidad = etCantidad.text.toString().trim().toIntOrNull()
        val fecha = etFecha.text.toString().trim()
        if (cantidad == null || cantidad <= 0 || fecha.isEmpty()) {
            mostrar("Escribe una cantidad y fecha válidas")
            return
        }

        val producto = productos[spProducto.selectedItemPosition]
        val tipo = if (rbEntrada.isChecked) "ENTRADA" else "SALIDA"

        val ok = db.registrarMovimiento(producto.id, tipo, cantidad, fecha)
        if (ok) {
            mostrar("$tipo registrada")
            etCantidad.setText("")
            etFecha.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
            actualizarPantalla()
        } else {
            mostrar("No hay cantidad suficiente")
        }
    }

    private fun actualizarPantalla() {
        val posicion = spProducto.selectedItemPosition

        productos = db.listarProductos()
        val nombres = ArrayList<String>()
        for (p in productos) {
            nombres.add(p.nombre + " (stock: " + p.cantidad + ")")
        }
        spProducto.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        if (posicion >= 0 && posicion < nombres.size) {
            spProducto.setSelection(posicion)
        }

        listaMovimientos.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, db.listarMovimientos())
    }

    private fun mostrar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
