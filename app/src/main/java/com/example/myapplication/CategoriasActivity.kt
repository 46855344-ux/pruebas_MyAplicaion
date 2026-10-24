package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoriasActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var etCodigo: EditText
    lateinit var etNombre: EditText
    lateinit var listaCategorias: ListView
    var categoriasList = ArrayList<String>()
    var categoriaSeleccionada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        db = DBHelper(this)
        etCodigo = findViewById(R.id.etCodigoCategoria)
        etNombre = findViewById(R.id.etNombreCategoria)
        listaCategorias = findViewById(R.id.listaCategorias)

        findViewById<Button>(R.id.btnAtras).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnCrear).setOnClickListener {
            val codigo = etCodigo.text.toString().trim().uppercase()
            val nombre = etNombre.text.toString().trim().lowercase()
            if (codigo.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Ingresa código y nombre de categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.agregarCategoria(codigo, nombre)
            Toast.makeText(this, "Categoría creada", Toast.LENGTH_SHORT).show()
            etCodigo.setText("")
            etNombre.setText("")
            actualizarLista()
        }

        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            val cat = categoriaSeleccionada
            if (cat == null) {
                Toast.makeText(this, "Selecciona una categoría de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nombreCat = cat.substringAfter("] ").trim()
            db.eliminarCategoria(nombreCat)
            Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show()
            categoriaSeleccionada = null
            etCodigo.setText("")
            etNombre.setText("")
            actualizarLista()
        }

        listaCategorias.setOnItemClickListener { _, _, position, _ ->
            categoriaSeleccionada = categoriasList[position]
            val item = categoriaSeleccionada ?: ""
            val cod = item.substringBetween("[", "]") ?: ""
            val nom = item.substringAfter("] ").trim()
            etCodigo.setText(cod)
            etNombre.setText("")
            etNombre.setText(nom)
        }

        actualizarLista()
    }

    private fun actualizarLista() {
        categoriasList = db.listarCategorias()
        listaCategorias.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoriasList)
    }

    private fun String.substringBetween(open: String, close: String): String? {
        val start = indexOf(open)
        if (start == -1) return null
        val end = indexOf(close, start + open.length)
        if (end == -1) return null
        return substring(start + open.length, end)
    }
}
