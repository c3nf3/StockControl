
package cl.stockcontrol.app

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper
import cl.stockcontrol.app.models.Product

class MovementActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var spProducts: Spinner
    private lateinit var edtQuantity: EditText
    private lateinit var rgMovement: RadioGroup

    private var products: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movement)

        databaseHelper = DatabaseHelper(this)

        spProducts = findViewById(R.id.spProducts)
        edtQuantity = findViewById(R.id.edtQuantity)
        rgMovement = findViewById(R.id.rgMovement)

        findViewById<RadioButton>(R.id.rbEntry).isChecked = true

        findViewById<Button>(R.id.btnMovementBack)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.btnRegisterMovement)
            .setOnClickListener {
                registerMovement()
            }

        loadProducts()
    }


    private fun loadProducts() {

        products = databaseHelper.getAllProducts()

        val productNames = products.map {
            "${it.name} (${it.code}) - Stock: ${it.stock}"
        }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            productNames
        ) {

            private fun createStyledView(
                position: Int,
                parent: android.view.ViewGroup,
                dropdown: Boolean
            ): TextView {

                return TextView(this@MovementActivity).apply {
                    text = productNames[position]
                    textSize = 16f

                    setTextColor(
                        android.graphics.Color.rgb(23, 54, 93)
                    )

                    gravity = android.view.Gravity.CENTER_VERTICAL

                    val padding = (14 * resources.displayMetrics.density).toInt()

                    setPadding(padding, padding, padding, padding)

                    minHeight = (dropdown.let {
                        if (it) 52 else 56
                    } * resources.displayMetrics.density).toInt()

                    if (dropdown) {
                        setBackgroundColor(android.graphics.Color.WHITE)
                    }
                }
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return createStyledView(position, parent, false)
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return createStyledView(position, parent, true)
            }
        }

        spProducts.adapter = adapter

        findViewById<Button>(R.id.btnRegisterMovement).isEnabled =
            products.isNotEmpty()

        if (products.isEmpty()) {
            Toast.makeText(
                this,
                "Primero debes registrar un producto",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun registerMovement() {

        val position = spProducts.selectedItemPosition

        if (position !in products.indices) {
            Toast.makeText(
                this,
                "Selecciona un producto válido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val quantity = edtQuantity.text.toString()
            .trim()
            .toIntOrNull()

        if (quantity == null || quantity <= 0) {
            edtQuantity.error = "Ingresa una cantidad mayor a cero"
            return
        }

        val type = when (rgMovement.checkedRadioButtonId) {
            R.id.rbEntry -> "ENTRADA"
            R.id.rbExit -> "SALIDA"
            else -> {
                Toast.makeText(
                    this,
                    "Selecciona entrada o salida",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        val product = products[position]

        val result = databaseHelper.registerMovement(
            product.id,
            type,
            quantity
        )

        when (result) {

            "SUCCESS" -> {

                Toast.makeText(
                    this,
                    "Movimiento registrado correctamente",
                    Toast.LENGTH_LONG
                ).show()

                edtQuantity.text.clear()

                loadProducts()

            }

            "INSUFFICIENT_STOCK" -> {

                Toast.makeText(
                    this,
                    "No hay suficientes unidades disponibles",
                    Toast.LENGTH_LONG
                ).show()
            }

            "NOT_FOUND" -> {

                Toast.makeText(
                    this,
                    "El producto ya no existe",
                    Toast.LENGTH_LONG
                ).show()

                loadProducts()
            }

            "INVALID_QUANTITY" -> {
                edtQuantity.error = "Cantidad inválida"
            }

            "STOCK_LIMIT" -> {
                Toast.makeText(
                    this,
                    "Se supera el límite de existencias",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {

                Toast.makeText(
                    this,
                    "No se pudo registrar el movimiento",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}
