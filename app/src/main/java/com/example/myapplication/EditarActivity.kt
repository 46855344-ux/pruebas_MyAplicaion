package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditarActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var spProductos: Spinner
    lateinit var etNombre: EditText
    lateinit var etPrecioUnidad: EditText
    lateinit var etCantidad: EditText
    lateinit var etVenta: EditText
    lateinit var etPrecioMayor: EditText
    lateinit var etFecha: EditText
    lateinit var spCategoria: Spinner
    var productos = ArrayList<Producto>()
    var idCargado = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar)

        db = DBHelper(this)
        spProductos = findViewById(R.id.spProductos)
        etNombre = findViewById(R.id.etNombre)
        etPrecioUnidad = findViewById(R.id.etPrecioUnidad)
        etCantidad = findViewById(R.id.etCantidad)
        etVenta = findViewById(R.id.etVenta)
        etPrecioMayor = findViewById(R.id.etPrecioMayor)
        etFecha = findViewById(R.id.etFecha)
        spCategoria = findViewById(R.id.spCategoria)

        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, db.listarCategorias())

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCargar).setOnClickListener { cargarDatos() }
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        findViewById<Button>(R.id.btnEliminar).setOnClickListener { eliminar() }

        actualizarSpinner()
    }

    private fun actualizarSpinner() {
        productos = db.listarProductos()
        val nombres = ArrayList<String>()
        for (p in productos) {
            nombres.add(p.nombre)
        }
        spProductos.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
    }

    private fun cargarDatos() {
        if (productos.isEmpty()) {
            mostrar("No hay productos registrados")
            return
        }
        val p = productos[spProductos.selectedItemPosition]
        idCargado = p.id
        etNombre.setText(p.nombre)
        etPrecioUnidad.setText(p.precioUnidad.toString())
        etCantidad.setText(p.cantidad.toString())
        etVenta.setText(p.venta.toString())
        etPrecioMayor.setText(p.precioMayor.toString())
        etFecha.setText(p.fecha)
        val cats = db.listarCategorias()
        spCategoria.setSelection(cats.indexOf(p.categoria).coerceAtLeast(0))
    }

    private fun guardar() {
        if (idCargado == -1) {
            mostrar("Primero presiona Cargar Datos")
            return
        }

        val nombre = texto(etNombre)
        val precioUnidad = texto(etPrecioUnidad).replace(",", ".").toDoubleOrNull()
        val cantidad = texto(etCantidad).toIntOrNull()
        val venta = texto(etVenta).replace(",", ".").toDoubleOrNull()
        val precioMayor = texto(etPrecioMayor).replace(",", ".").toDoubleOrNull()
        val fecha = texto(etFecha)

        if (nombre.isEmpty() || precioUnidad == null || cantidad == null ||
            venta == null || precioMayor == null || fecha.isEmpty()) {
            mostrar("Completa todos los campos con datos válidos")
            return
        }

        val cats = db.listarCategorias()
        db.editarProducto(Producto(idCargado, nombre, precioUnidad, cantidad, venta, precioMayor,
            cats[spCategoria.selectedItemPosition], fecha))
        mostrar("Producto actualizado")
        limpiar()
    }

    private fun eliminar() {
        if (idCargado == -1) {
            mostrar("Primero presiona Cargar Datos")
            return
        }
        db.eliminarProducto(idCargado)
        mostrar("Producto eliminado")
        limpiar()
    }

    private fun limpiar() {
        idCargado = -1
        etNombre.setText("")
        etPrecioUnidad.setText("")
        etCantidad.setText("")
        etVenta.setText("")
        etPrecioMayor.setText("")
        etFecha.setText("")
        actualizarSpinner()
    }

    private fun texto(campo: EditText): String {
        return campo.text.toString().trim()
    }

    private fun mostrar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
