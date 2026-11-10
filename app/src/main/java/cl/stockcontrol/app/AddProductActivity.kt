
package cl.stockcontrol.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper
import cl.stockcontrol.app.models.Product

class AddProductActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var editingProductId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        databaseHelper = DatabaseHelper(this)

        val edtCode = findViewById<EditText>(R.id.edtCode)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtPrice = findViewById<EditText>(R.id.edtPrice)
        val edtStock = findViewById<EditText>(R.id.edtStock)
        val edtMinStock = findViewById<EditText>(R.id.edtMinStock)

        val btnSave = findViewById<Button>(R.id.btnSaveProduct)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        val title = findViewById<TextView>(
            resources.getIdentifier(
                "formTitle", "id", packageName
            )
        )

        editingProductId = intent.getIntExtra("product_id", -1)

        // Si existe un ID, estamos editando.
        if (editingProductId != -1) {

            val product = databaseHelper.getAllProducts()
                .find { it.id == editingProductId }

            if (product == null) {
                Toast.makeText(
                    this,
                    "No se encontró el producto",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }

            edtCode.setText(product.code)
            edtName.setText(product.name)
            edtPrice.setText(product.price.toString())
            edtStock.setText(product.stock.toString())
            edtMinStock.setText(product.minStock.toString())

            btnSave.text = "Actualizar producto"
            title?.text = "Editar producto"
        }

        btnSave.setOnClickListener {

            val code = edtCode.text.toString()
                .trim().uppercase()

            val name = edtName.text.toString().trim()

            val priceText = edtPrice.text.toString()
                .trim().replace(",", ".")

            val stockText = edtStock.text.toString().trim()
            val minStockText = edtMinStock.text.toString().trim()

            if (code.isEmpty() || name.isEmpty() ||
                priceText.isEmpty() || stockText.isEmpty() ||
                minStockText.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            val stock = stockText.toIntOrNull()
            val minStock = minStockText.toIntOrNull()

            if (price == null || stock == null || minStock == null) {
                Toast.makeText(
                    this,
                    "Ingresa valores numéricos válidos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!price.isFinite() || price <= 0 ||
                stock < 0 || minStock < 0
            ) {
                Toast.makeText(
                    this,
                    "Revisa el precio y las cantidades",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val product = Product(
                id = editingProductId.coerceAtLeast(0),
                code = code,
                name = name,
                price = price,
                stock = stock,
                minStock = minStock
            )

            val result = if (editingProductId == -1) {
                databaseHelper.insertProduct(product)
                    .let { if (it == -1L) -1 else 1 }
            } else {
                databaseHelper.updateProduct(product)
            }

            if (result > 0) {

                val message = if (editingProductId == -1) {
                    "Producto registrado correctamente"
                } else {
                    "Producto actualizado correctamente"
                }

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                setResult(RESULT_OK)
                finish()

            } else {

                Toast.makeText(
                    this,
                    "No se pudo guardar. Verifica que el código no esté duplicado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}
