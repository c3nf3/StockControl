
package cl.stockcontrol.app.database

import cl.stockcontrol.app.models.Movement
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.stockcontrol.app.models.Product

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val TABLE_MOVEMENTS = "movements"

        private const val COL_MOVEMENT_ID = "id"
        private const val COL_PRODUCT_ID = "product_id"
        private const val COL_MOVEMENT_TYPE = "type"
        private const val COL_QUANTITY = "quantity"
        private const val COL_DATE = "date"
        private const val DATABASE_NAME = "stockcontrol.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_PRODUCTS = "products"

        private const val COL_ID = "id"
        private const val COL_CODE = "code"
        private const val COL_NAME = "name"
        private const val COL_PRICE = "price"
        private const val COL_STOCK = "stock"
        private const val COL_MIN_STOCK = "min_stock"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CODE TEXT NOT NULL UNIQUE COLLATE NOCASE,
                $COL_NAME TEXT NOT NULL,
                $COL_PRICE REAL NOT NULL,
                $COL_STOCK INTEGER NOT NULL,
                $COL_MIN_STOCK INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createProductsTable)

        val createMovementsTable = """
    CREATE TABLE $TABLE_MOVEMENTS (
        $COL_MOVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_PRODUCT_ID INTEGER NOT NULL,
        $COL_MOVEMENT_TYPE TEXT NOT NULL
            CHECK ($COL_MOVEMENT_TYPE IN ('ENTRADA', 'SALIDA')),
        $COL_QUANTITY INTEGER NOT NULL
            CHECK ($COL_QUANTITY > 0),
        $COL_DATE TEXT NOT NULL,
        FOREIGN KEY ($COL_PRODUCT_ID)
            REFERENCES $TABLE_PRODUCTS($COL_ID)
            ON DELETE CASCADE
    )
""".trimIndent()

        db.execSQL(createMovementsTable)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {

            val createMovementsTable = """
            CREATE TABLE $TABLE_MOVEMENTS (
                $COL_MOVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PRODUCT_ID INTEGER NOT NULL,
                $COL_MOVEMENT_TYPE TEXT NOT NULL
                    CHECK ($COL_MOVEMENT_TYPE IN ('ENTRADA', 'SALIDA')),
                $COL_QUANTITY INTEGER NOT NULL
                    CHECK ($COL_QUANTITY > 0),
                $COL_DATE TEXT NOT NULL,
                FOREIGN KEY ($COL_PRODUCT_ID)
                    REFERENCES $TABLE_PRODUCTS($COL_ID)
                    ON DELETE CASCADE
            )
        """.trimIndent()

            db.execSQL(createMovementsTable)
        }
    }

    fun insertProduct(product: Product): Long {

        val values = ContentValues().apply {
            put(COL_CODE, product.code)
            put(COL_NAME, product.name)
            put(COL_PRICE, product.price)
            put(COL_STOCK, product.stock)
            put(COL_MIN_STOCK, product.minStock)
        }

        return try {
            writableDatabase.insertOrThrow(
                TABLE_PRODUCTS,
                null,
                values
            )
        } catch (e: SQLiteConstraintException) {
            -1L
        }
    }

    fun getProductCount(): Int {

        val query = "SELECT COUNT(*) FROM $TABLE_PRODUCTS"

        readableDatabase.rawQuery(query, null).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        }
    }

    fun getLowStockCount(): Int {

        val query = """
            SELECT COUNT(*)
            FROM $TABLE_PRODUCTS
            WHERE $COL_STOCK <= $COL_MIN_STOCK
        """.trimIndent()

        readableDatabase.rawQuery(query, null).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        }
    }

    fun getLowStockProducts(): List<Product> {

        val products = mutableListOf<Product>()

        val query = """
        SELECT *
        FROM $TABLE_PRODUCTS
        WHERE $COL_STOCK <= $COL_MIN_STOCK
        ORDER BY $COL_STOCK ASC
    """.trimIndent()

        readableDatabase.rawQuery(query, null).use { cursor ->

            while (cursor.moveToNext()) {

                val product = Product(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_ID)
                    ),
                    code = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CODE)
                    ),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME)
                    ),
                    price = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_PRICE)
                    ),
                    stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_STOCK)
                    ),
                    minStock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_MIN_STOCK)
                    )
                )

                products.add(product)
            }
        }

        return products
    }

    fun getAllProducts(): List<Product> {

        val products = mutableListOf<Product>()

        val query = """
        SELECT * FROM $TABLE_PRODUCTS
        ORDER BY $COL_NAME ASC
    """.trimIndent()

        readableDatabase.rawQuery(query, null).use { cursor ->

            while (cursor.moveToNext()) {

                val product = Product(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_ID)
                    ),
                    code = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CODE)
                    ),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_NAME)
                    ),
                    price = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_PRICE)
                    ),
                    stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_STOCK)
                    ),
                    minStock = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_MIN_STOCK)
                    )
                )

                products.add(product)
            }
        }

        return products
    }

    fun updateProduct(product: Product): Int {

        val values = ContentValues().apply {
            put(COL_CODE, product.code)
            put(COL_NAME, product.name)
            put(COL_PRICE, product.price)
            put(COL_STOCK, product.stock)
            put(COL_MIN_STOCK, product.minStock)
        }

        return try {
            writableDatabase.update(
                TABLE_PRODUCTS,
                values,
                "$COL_ID = ?",
                arrayOf(product.id.toString())
            )
        } catch (e: SQLiteConstraintException) {
            -1
        }
    }

    fun deleteProduct(productId: Int): Int {

        return writableDatabase.delete(
            TABLE_PRODUCTS,
            "$COL_ID = ?",
            arrayOf(productId.toString())
        )
    }


    fun registerMovement(
        productId: Int,
        type: String,
        quantity: Int
    ): String {

        if (type != "ENTRADA" && type != "SALIDA") {
            return "INVALID_TYPE"
        }

        if (quantity <= 0) {
            return "INVALID_QUANTITY"
        }

        val db = writableDatabase

        return try {

            db.beginTransaction()

            val cursor = db.rawQuery(
                "SELECT $COL_STOCK FROM $TABLE_PRODUCTS WHERE $COL_ID = ?",
                arrayOf(productId.toString())
            )

            val currentStock = cursor.use {
                if (it.moveToFirst()) it.getInt(0) else null
            }

            if (currentStock == null) {
                return "NOT_FOUND"
            }

            val newStock = if (type == "ENTRADA") {
                currentStock.toLong() + quantity.toLong()
            } else {
                currentStock.toLong() - quantity.toLong()
            }

            if (newStock < 0) {
                return "INSUFFICIENT_STOCK"
            }

            if (newStock > Int.MAX_VALUE) {
                return "STOCK_LIMIT"
            }

            val updateValues = ContentValues().apply {
                put(COL_STOCK, newStock.toInt())
            }

            db.update(
                TABLE_PRODUCTS,
                updateValues,
                "$COL_ID = ?",
                arrayOf(productId.toString())
            )

            val movementValues = ContentValues().apply {
                put(COL_PRODUCT_ID, productId)
                put(COL_MOVEMENT_TYPE, type)
                put(COL_QUANTITY, quantity)
                put(
                    COL_DATE,
                    java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())
                )
            }

            db.insertOrThrow(
                TABLE_MOVEMENTS,
                null,
                movementValues
            )

            db.setTransactionSuccessful()

            "SUCCESS"

        } catch (e: android.database.sqlite.SQLiteException) {
            "DATABASE_ERROR"

        } finally {
            db.endTransaction()
        }
    }

    fun getAllMovements(): List<Movement> {

        val movements = mutableListOf<Movement>()

        val query = """
        SELECT
            m.id,
            p.name,
            p.code,
            m.type,
            m.quantity,
            m.date
        FROM $TABLE_MOVEMENTS m
        INNER JOIN $TABLE_PRODUCTS p
            ON m.$COL_PRODUCT_ID = p.$COL_ID
        ORDER BY m.$COL_MOVEMENT_ID DESC
    """.trimIndent()

        readableDatabase.rawQuery(query, null).use { cursor ->

            while (cursor.moveToNext()) {

                val movement = Movement(
                    id = cursor.getInt(0),
                    productName = cursor.getString(1),
                    productCode = cursor.getString(2),
                    type = cursor.getString(3),
                    quantity = cursor.getInt(4),
                    date = cursor.getString(5)
                )

                movements.add(movement)
            }
        }

        return movements
    }



}
