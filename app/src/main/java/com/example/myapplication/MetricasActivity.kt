package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MetricasActivity : AppCompatActivity() {

    lateinit var db: DBHelper
    lateinit var tvTotalProductos: TextView
    lateinit var spAgotados: Spinner
    lateinit var spStockMinimo: Spinner
    lateinit var tvValorInventario: TextView
    lateinit var spSinMovimiento: Spinner
    lateinit var tvMasSalidaSemanal: TextView
    lateinit var tvMasSalidaMensual: TextView
    lateinit var listaEntradas: ListView
    lateinit var listaSalidas: ListView
    lateinit var spDetalleProducto: Spinner
    lateinit var spDetalleCategoria: Spinner
    lateinit var tvDetalleStock: TextView
    lateinit var tvDetalleFecha: TextView
    lateinit var listaResumen: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metricas)

        db = DBHelper(this)

        findViewById<Button>(R.id.btnAtras).setOnClickListener { finish() }

        tvTotalProductos = findViewById(R.id.tvTotalProductos)
        spAgotados = findViewById(R.id.spAgotados)
        spStockMinimo = findViewById(R.id.spStockMinimo)
        tvValorInventario = findViewById(R.id.tvValorInventario)
        spSinMovimiento = findViewById(R.id.spSinMovimiento)
        tvMasSalidaSemanal = findViewById(R.id.tvMasSalidaSemanal)
        tvMasSalidaMensual = findViewById(R.id.tvMasSalidaMensual)
        listaEntradas = findViewById(R.id.listaEntradas)
        listaSalidas = findViewById(R.id.listaSalidas)
        spDetalleProducto = findViewById(R.id.spDetalleProducto)
        spDetalleCategoria = findViewById(R.id.spDetalleCategoria)
        tvDetalleStock = findViewById(R.id.tvDetalleStock)
        tvDetalleFecha = findViewById(R.id.tvDetalleFecha)
        listaResumen = findViewById(R.id.listaResumen)

        cargarMetricas()
    }

    private fun cargarMetricas() {
        val productos = db.listarProductos()

        // Total productos
        tvTotalProductos.text = "Total Productos: ${productos.size}"

        // Agotados [▼]
        val agotados = productos.filter { it.cantidad == 0 }.map { it.nombre }
        val adapterAgotados = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, if (agotados.isEmpty()) listOf("Ninguno") else agotados)
        spAgotados.adapter = adapterAgotados

        // Stock Mínimo (< 5)
        val stockMin = productos.filter { it.cantidad > 0 && it.cantidad < 5 }.map { "${it.nombre} (${it.cantidad}u)" }
        val adapterStockMin = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, if (stockMin.isEmpty()) listOf("Ninguno") else stockMin)
        spStockMinimo.adapter = adapterStockMin

        // Valor Total Inventario
        val valorTotal = productos.sumOf { it.cantidad * it.precioUnidad }
        tvValorInventario.text = "Valor Total Inventario: S/ %.2f".format(valorTotal)

        // Sin movimiento (30 días)
        val sinMov = listOf("Sin registros")
        spSinMovimiento.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sinMov)

        // Más Salidas
        val masVendido = productos.maxByOrNull { it.cantidad }?.nombre ?: "-"
        tvMasSalidaSemanal.text = "Semanal: $masVendido"
        tvMasSalidaMensual.text = "Mensual: $masVendido"

        // Entradas y Salidas con fecha
        listaEntradas.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, db.listarMovimientosPorTipo("ENTRADA"))
        listaSalidas.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, db.listarMovimientosPorTipo("SALIDA"))

        // Tabla Resumen: Producto | Categoría | Stock Act | Stock Mín | Estado
        val resumenItems = ArrayList<String>()
        for (p in productos) {
            val estado = if (p.cantidad == 0) "Agotado" else if (p.cantidad < 5) "Stock Bjo" else "Normal"
            resumenItems.add("${p.nombre} | ${p.categoria} | ${p.cantidad}u | Mín: 5 | $estado")
        }
        listaResumen.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, if (resumenItems.isEmpty()) listOf("No hay productos") else resumenItems)
    }
}
