package cl.stockcontrol.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper

class LowStockActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var containerAlerts: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_low_stock)

        databaseHelper = DatabaseHelper(this)
        containerAlerts = findViewById(R.id.containerAlerts)

        loadAlerts()
    }


    private fun loadAlerts() {

        containerAlerts.removeAllViews()

        try {
            val products = databaseHelper.getLowStockProducts()

            val padding = (20 * resources.displayMetrics.density).toInt()

            if (products.isEmpty()) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(padding, padding, padding, padding)
                    background = androidx.core.content.ContextCompat.getDrawable(
                        this@LowStockActivity,
                        R.drawable.bg_card
                    )
                }

                val message = TextView(this).apply {
                    text = "¡Excelente! No hay productos con stock bajo."
                    textSize = 17f
                    setTextColor(android.graphics.Color.rgb(22, 131, 74))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                card.addView(message)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(0, padding, 0, padding)
                containerAlerts.addView(card, params)

                return
            }

            for (product in products) {

                val status = if (product.stock == 0) {
                    "SIN EXISTENCIAS"
                } else if (product.stock < product.minStock) {
                    "STOCK BAJO"
                } else {
                    "STOCK MÍNIMO"
                }

                val alert = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(padding, padding, padding, padding)

                    background = androidx.core.content.ContextCompat.getDrawable(
                        this@LowStockActivity,
                        R.drawable.bg_card
                    )
                }

                val title = TextView(this).apply {
                    text = product.name
                    textSize = 20f
                    setTextColor(android.graphics.Color.rgb(23, 54, 93))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                val code = TextView(this).apply {
                    text = "Código: ${product.code}"
                    textSize = 15f
                    setTextColor(android.graphics.Color.rgb(71, 84, 103))
                }

                val stock = TextView(this).apply {
                    text = "Stock actual: ${product.stock}"
                    textSize = 17f
                    setTextColor(
                        if (product.stock == 0) {
                            android.graphics.Color.rgb(209, 67, 67)
                        } else {
                            android.graphics.Color.rgb(217, 119, 6)
                        }
                    )
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                val minimum = TextView(this).apply {
                    text = "Stock mínimo: ${product.minStock}"
                    textSize = 15f
                    setTextColor(android.graphics.Color.rgb(71, 84, 103))
                }

                val statusView = TextView(this).apply {
                    text = status
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)

                    setTextColor(
                        if (product.stock == 0) {
                            android.graphics.Color.rgb(209, 67, 67)
                        } else {
                            android.graphics.Color.rgb(217, 119, 6)
                        }
                    )
                }

                alert.addView(title)
                alert.addView(code)
                alert.addView(stock)
                alert.addView(minimum)
                alert.addView(statusView)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(0, 0, 0, padding)
                containerAlerts.addView(alert, params)
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar las alertas",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}