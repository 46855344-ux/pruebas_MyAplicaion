package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductosActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var etNombre: EditText
    lateinit var etPrecioUnidad: EditText
    lateinit var etVenta: EditText
    lateinit var etPrecioMayor: EditText
    lateinit var etStock: EditText
    lateinit var etFecha: EditText
    lateinit var spCategoria: Spinner
    lateinit var lista: ListView

    var productos = ArrayList<Producto>()
    var idSeleccionado = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        db = DBHelper(this)
        etNombre = findViewById(R.id.etNombre)
        etPrecioUnidad = findViewById(R.id.etPrecioUnidad)
        etVenta = findViewById(R.id.etVenta)
        etPrecioMayor = findViewById(R.id.etPrecioMayor)
        etStock = findViewById(R.id.etStock)
        etFecha = findViewById(R.id.etFecha)
        spCategoria = findViewById(R.id.spCategoria)
        lista = findViewById(R.id.listaProductos)

        val btnAtras = findViewById<Button>(R.id.btnAtras)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        btnAtras.setOnClickListener { finish() }

        val cats = db.listarCategorias()
        spCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cats)

        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etFecha.setText(fechaHoy)

        lista.setOnItemClickListener { _, _, posicion, _ ->
            val p = productos[posicion]
            idSeleccionado = p.id
            etNombre.setText(p.nombre)
            etPrecioUnidad.setText(p.precioUnidad.toString())
            etVenta.setText(p.venta.toString())
            etPrecioMayor.setText(p.precioMayor.toString())
            etStock.setText(p.cantidad.toString())
            etFecha.setText(p.fecha)

            val catIndex = cats.indexOf(p.categoria)
            if (catIndex >= 0) {
                spCategoria.setSelection(catIndex)
            }
        }

        btnGuardar.setOnClickListener {
            if (camposVacios()) return@setOnClickListener
            val precioUnidad = texto(etPrecioUnidad).replace(",", ".").toDoubleOrNull()
            val venta = texto(etVenta).replace(",", ".").toDoubleOrNull()
            val precioMayor = texto(etPrecioMayor).replace(",", ".").toDoubleOrNull()
            val stock = texto(etStock).toIntOrNull()
            val fecha = texto(etFecha)

            if (precioUnidad == null || venta == null || precioMayor == null || stock == null || fecha.isEmpty()) {
                mostrar("Por favor, ingresa números válidos y fecha")
                return@setOnClickListener
            }

            val p = Producto(
                id = 0,
                nombre = texto(etNombre),
                precioUnidad = precioUnidad,
                cantidad = stock,
                venta = venta,
                precioMayor = precioMayor,
                categoria = spCategoria.selectedItem.toString(),
                fecha = fecha
            )
            db.agregarProducto(p)
            mostrar("Producto guardado")
            limpiar()
        }

        btnEditar.setOnClickListener {
            if (idSeleccionado == -1) {
                mostrar("Toca un producto de la lista para editar")
                return@setOnClickListener
            }
            if (camposVacios()) return@setOnClickListener
            val precioUnidad = texto(etPrecioUnidad).replace(",", ".").toDoubleOrNull()
            val venta = texto(etVenta).replace(",", ".").toDoubleOrNull()
            val precioMayor = texto(etPrecioMayor).replace(",", ".").toDoubleOrNull()
            val stock = texto(etStock).toIntOrNull()
            val fecha = texto(etFecha)

            if (precioUnidad == null || venta == null || precioMayor == null || stock == null || fecha.isEmpty()) {
                mostrar("Por favor, ingresa números válidos y fecha")
                return@setOnClickListener
            }

            val p = Producto(
                id = idSeleccionado,
                nombre = texto(etNombre),
                precioUnidad = precioUnidad,
                cantidad = stock,
                venta = venta,
                precioMayor = precioMayor,
                categoria = spCategoria.selectedItem.toString(),
                fecha = fecha
            )
            db.editarProducto(p)
            mostrar("Producto editado")
            limpiar()
        }

        btnEliminar.setOnClickListener {
            if (idSeleccionado == -1) {
                mostrar("Toca un producto de la lista para eliminar")
                return@setOnClickListener
            }
            db.eliminarProducto(idSeleccionado)
            mostrar("Producto eliminado")
            limpiar()
        }

        mostrarLista()
    }

    private fun mostrarLista() {
        productos = db.listarProductos()
        val textos = ArrayList<String>()
        for (p in productos) {
            textos.add("${p.nombre}  |  Stock: ${p.cantidad}  |  S/ ${p.venta} (${p.categoria}) [${p.fecha}]")
        }
        lista.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, textos)
    }

    private fun texto(campo: EditText): String {
        return campo.text.toString().trim()
    }

    private fun camposVacios(): Boolean {
        if (texto(etNombre).isEmpty() || texto(etPrecioUnidad).isEmpty() ||
            texto(etVenta).isEmpty() || texto(etPrecioMayor).isEmpty() || texto(etStock).isEmpty() || texto(etFecha).isEmpty()) {
            mostrar("Completa todos los campos")
            return true
        }
        return false
    }

    private fun limpiar() {
        idSeleccionado = -1
        etNombre.setText("")
        etPrecioUnidad.setText("")
        etVenta.setText("")
        etPrecioMayor.setText("")
        etStock.setText("")
        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etFecha.setText(fechaHoy)
        spCategoria.setSelection(0)
        mostrarLista()
    }

    private fun mostrar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
