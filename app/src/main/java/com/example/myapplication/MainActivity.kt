package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAgregar = findViewById<Button>(R.id.btnAgregar)
        val btnCategorias = findViewById<Button>(R.id.btnCategorias)
        val btnMovimientos = findViewById<Button>(R.id.btnMovimientos)
        val btnMetricas = findViewById<Button>(R.id.btnMetricas)

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, ProductosActivity::class.java))
        }
        btnCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }
        btnMovimientos.setOnClickListener {
            startActivity(Intent(this, MovimientosActivity::class.java))
        }
        btnMetricas.setOnClickListener {
            startActivity(Intent(this, MetricasActivity::class.java))
        }
    }
}
