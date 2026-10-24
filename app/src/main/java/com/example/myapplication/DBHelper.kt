package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

val categorias = listOf("pasamaneria/adornos", "botones", "pinturas", "hilos", "otros")

data class Producto(
    val id: Int,
    val nombre: String,
    val precioUnidad: Double,
    val cantidad: Int,
    val venta: Double,
    val precioMayor: Double,
    val categoria: String,
    val fecha: String
)

class DBHelper(context: Context) : SQLiteOpenHelper(context, "inventario.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE categorias(id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT, nombre TEXT)")
        db.execSQL("CREATE TABLE productos(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, precio_unidad REAL, cantidad INTEGER, " +
                "venta REAL, precio_mayor REAL, categoria TEXT, fecha TEXT)")
        db.execSQL("CREATE TABLE movimientos(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "producto_id INTEGER, tipo TEXT, cantidad INTEGER, fecha TEXT)")

        val defaults = listOf(
            Pair("PAS", "pasamaneria/adornos"),
            Pair("BOT", "botones"),
            Pair("PIN", "pinturas"),
            Pair("HIL", "hilos"),
            Pair("OTR", "otros")
        )
        for (pair in defaults) {
            val cv = ContentValues()
            cv.put("codigo", pair.first)
            cv.put("nombre", pair.second)
            db.insert("categorias", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, viejo: Int, nuevo: Int) {
        db.execSQL("DROP TABLE IF EXISTS productos")
        db.execSQL("DROP TABLE IF EXISTS movimientos")
        db.execSQL("DROP TABLE IF EXISTS categorias")
        onCreate(db)
    }

    fun listarCategorias(): ArrayList<String> {
        val lista = ArrayList<String>()
        val c = readableDatabase.rawQuery("SELECT codigo, nombre FROM categorias ORDER BY nombre", null)
        while (c.moveToNext()) {
            val codigo = c.getString(0) ?: ""
            val nombre = c.getString(1) ?: ""
            lista.add("[$codigo] $nombre")
        }
        c.close()
        if (lista.isEmpty()) {
            lista.addAll(listOf("[PAS] pasamaneria/adornos", "[BOT] botones", "[PIN] pinturas", "[HIL] hilos", "[OTR] otros"))
        }
        return lista
    }

    fun listarNombresCategorias(): ArrayList<String> {
        val lista = ArrayList<String>()
        val c = readableDatabase.rawQuery("SELECT nombre FROM categorias ORDER BY nombre", null)
        while (c.moveToNext()) {
            lista.add(c.getString(0))
        }
        c.close()
        if (lista.isEmpty()) {
            lista.addAll(listOf("pasamaneria/adornos", "botones", "pinturas", "hilos", "otros"))
        }
        return lista
    }

    fun agregarCategoria(codigo: String, nombre: String) {
        val cv = ContentValues()
        cv.put("codigo", codigo)
        cv.put("nombre", nombre)
        writableDatabase.insert("categorias", null, cv)
    }

    fun eliminarCategoria(nombre: String) {
        writableDatabase.delete("categorias", "nombre=?", arrayOf(nombre))
    }

    private fun valores(p: Producto): ContentValues {
        val datos = ContentValues()
        datos.put("nombre", p.nombre)
        datos.put("precio_unidad", p.precioUnidad)
        datos.put("cantidad", p.cantidad)
        datos.put("venta", p.venta)
        datos.put("precio_mayor", p.precioMayor)
        datos.put("categoria", p.categoria)
        datos.put("fecha", p.fecha)
        return datos
    }

    fun agregarProducto(p: Producto) {
        writableDatabase.insert("productos", null, valores(p))
    }

    fun listarProductos(): ArrayList<Producto> {
        val lista = ArrayList<Producto>()
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, precio_unidad, cantidad, venta, precio_mayor, categoria, fecha " +
                    "FROM productos ORDER BY nombre", null)
        while (c.moveToNext()) {
            lista.add(Producto(
                c.getInt(0),
                c.getString(1),
                c.getDouble(2),
                c.getInt(3),
                c.getDouble(4),
                c.getDouble(5),
                c.getString(6),
                c.getString(7) ?: ""
            ))
        }
        c.close()
        return lista
    }

    fun editarProducto(p: Producto) {
        writableDatabase.update("productos", valores(p), "id=?", arrayOf(p.id.toString()))
    }

    fun eliminarProducto(id: Int) {
        writableDatabase.delete("movimientos", "producto_id=?", arrayOf(id.toString()))
        writableDatabase.delete("productos", "id=?", arrayOf(id.toString()))
    }

    fun registrarMovimiento(productoId: Int, tipo: String, cantidad: Int, fecha: String): Boolean {
        val db = writableDatabase
        val c = db.rawQuery("SELECT cantidad FROM productos WHERE id=?", arrayOf(productoId.toString()))
        c.moveToFirst()
        val cantidadActual = c.getInt(0)
        c.close()

        val cantidadNueva = if (tipo == "ENTRADA") cantidadActual + cantidad else cantidadActual - cantidad
        if (cantidadNueva < 0) {
            return false
        }

        val datos = ContentValues()
        datos.put("cantidad", cantidadNueva)
        db.update("productos", datos, "id=?", arrayOf(productoId.toString()))

        val mov = ContentValues()
        mov.put("producto_id", productoId)
        mov.put("tipo", tipo)
        mov.put("cantidad", cantidad)
        mov.put("fecha", fecha)
        db.insert("movimientos", null, mov)

        return true
    }

    fun listarMovimientos(): ArrayList<String> {
        val lista = ArrayList<String>()
        val c = readableDatabase.rawQuery(
            "SELECT m.tipo, p.nombre, m.cantidad, m.fecha FROM movimientos m " +
                    "JOIN productos p ON p.id = m.producto_id ORDER BY m.id DESC", null)
        while (c.moveToNext()) {
            val fechaStr = if (c.isNull(3)) "" else " [${c.getString(3)}]"
            lista.add(c.getString(0) + " - " + c.getString(1) + " x" + c.getInt(2) + fechaStr)
        }
        c.close()
        return lista
    }

    fun listarMovimientosPorTipo(tipoBusqueda: String): ArrayList<String> {
        val lista = ArrayList<String>()
        val c = readableDatabase.rawQuery(
            "SELECT p.nombre, m.cantidad, m.fecha FROM movimientos m " +
                    "JOIN productos p ON p.id = m.producto_id WHERE m.tipo=? ORDER BY m.id DESC", arrayOf(tipoBusqueda))
        while (c.moveToNext()) {
            val fechaStr = if (c.isNull(2)) "" else " - Fecha: ${c.getString(2)}"
            lista.add("${c.getString(0)} (${c.getInt(1)}u.)$fechaStr")
        }
        c.close()
        return lista
    }

    private fun valor(sql: String): String {
        val c = readableDatabase.rawQuery(sql, null)
        var resultado = "-"
        if (c.moveToFirst() && !c.isNull(0)) {
            resultado = c.getString(0)
        }
        c.close()
        return resultado
    }

    fun metricas(): ArrayList<String> {
        val m = ArrayList<String>()
        m.add("1. Total de productos: " + valor("SELECT COUNT(*) FROM productos"))
        m.add("2. Unidades en stock: " + valor("SELECT SUM(cantidad) FROM productos"))
        m.add("3. Valor del inventario (a precio de compra): S/ " + valor("SELECT ROUND(SUM(cantidad*precio_unidad),2) FROM productos"))
        m.add("4. Ganancia potencial (si se vende todo): S/ " + valor("SELECT ROUND(SUM(cantidad*(venta-precio_unidad)),2) FROM productos"))
        m.add("5. Productos agotados: " + valor("SELECT COUNT(*) FROM productos WHERE cantidad=0"))
        m.add("6. Categoría con más unidades: " + valor("SELECT categoria || ' (' || SUM(cantidad) || ' u.)' FROM productos GROUP BY categoria HAVING SUM(cantidad)>0 ORDER BY SUM(cantidad) DESC LIMIT 1"))
        m.add("7. Producto con mayor margen: " + valor("SELECT nombre || ' (S/ ' || ROUND(venta-precio_unidad,2) || ' por unidad)' FROM productos ORDER BY (venta-precio_unidad) DESC LIMIT 1"))
        m.add("8. Producto más vendido: " + valor("SELECT p.nombre || ' (' || SUM(m.cantidad) || ' u.)' FROM movimientos m JOIN productos p ON p.id = m.producto_id WHERE m.tipo='SALIDA' GROUP BY p.id ORDER BY SUM(m.cantidad) DESC LIMIT 1"))
        return m
    }
}
