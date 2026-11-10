
package cl.stockcontrol.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper
import cl.stockcontrol.app.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductListActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var container: LinearLayout
    private lateinit var txtEmpty: TextView
    private lateinit var txtCount: TextView
    private lateinit var edtSearch: EditText

    private var products: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        databaseHelper = DatabaseHelper(this)

        container = findViewById(R.id.productContainer)
        txtEmpty = findViewById(R.id.txtEmpty)
        txtCount = findViewById(R.id.txtProductCount)
        edtSearch = findViewById(R.id.edtSearch)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        edtSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                displayProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun loadProducts() {
        products = databaseHelper.getAllProducts()
        txtCount.text = "Productos: ${products.size}"
        displayProducts(edtSearch.text.toString())
    }

    private fun displayProducts(search: String) {

        container.removeAllViews()

        val filtered = products.filter {
            it.name.contains(search, ignoreCase = true) ||
                    it.code.contains(search, ignoreCase = true)
        }

        txtEmpty.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        txtEmpty.text =
            if (products.isEmpty()) {
                "No hay productos registrados"
            } else {
                "No se encontraron productos"
            }

        for (product in filtered) {
            container.addView(createProductCard(product))
        }
    }


    private fun createProductCard(product: Product): View {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = androidx.core.content.ContextCompat.getDrawable(
                this@ProductListActivity,
                R.drawable.bg_card
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.bottomMargin = 16
            layoutParams = params
        }

        val currency = NumberFormat.getCurrencyInstance(
            Locale("es", "CL")
        )

        val title = TextView(this).apply {
            text = product.name
            textSize = 21f
            setTextColor(android.graphics.Color.rgb(23, 54, 93))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val code = TextView(this).apply {
            text = "Código: ${product.code}"
            textSize = 15f
            setTextColor(android.graphics.Color.rgb(71, 84, 103))
        }

        val price = TextView(this).apply {
            text = "Precio: ${currency.format(product.price)}"
            textSize = 15f
            setTextColor(android.graphics.Color.rgb(71, 84, 103))
        }

        val stock = TextView(this).apply {
            text = if (product.stock == 1) {
                "Existencias: 1 unidad"
            } else {
                "Existencias: ${product.stock} unidades"
            }

            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)

            setTextColor(
                if (product.stock <= product.minStock) {
                    android.graphics.Color.rgb(217, 119, 6)
                } else {
                    android.graphics.Color.rgb(22, 131, 74)
                }
            )
        }

        val minimum = TextView(this).apply {
            text = "Stock mínimo: ${product.minStock}"
            textSize = 14f
            setTextColor(android.graphics.Color.rgb(71, 84, 103))
        }

        val status = TextView(this).apply {
            text = if (product.stock <= product.minStock) {
                "STOCK BAJO"
            } else {
                "DISPONIBLE"
            }

            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)

            setTextColor(
                if (product.stock <= product.minStock) {
                    android.graphics.Color.rgb(217, 119, 6)
                } else {
                    android.graphics.Color.rgb(22, 131, 74)
                }
            )
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        val btnEdit = Button(this).apply {
            text = "Editar"
            isAllCaps = false
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(23, 54, 93)
                )

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 6
            }
        }

        val btnDelete = Button(this).apply {
            text = "Eliminar"
            isAllCaps = false
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(217, 67, 67)
                )

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = 6
            }
        }

        btnEdit.setOnClickListener {
            val intent = Intent(
                this,
                AddProductActivity::class.java
            )
            intent.putExtra("product_id", product.id)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage(
                    "¿Estás seguro de que deseas eliminar ${product.name}?"
                )
                .setPositiveButton("Eliminar") { _, _ ->
                    val result = databaseHelper.deleteProduct(product.id)

                    if (result > 0) {
                        loadProducts()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        actions.addView(btnEdit)
        actions.addView(btnDelete)

        card.addView(title)
        card.addView(code)
        card.addView(price)
        card.addView(stock)
        card.addView(minimum)
        card.addView(status)
        card.addView(actions)

        return card
    }


    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}
