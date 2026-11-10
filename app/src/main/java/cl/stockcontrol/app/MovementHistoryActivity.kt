package cl.stockcontrol.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper

class MovementHistoryActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var containerMovements: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movement_history)

        databaseHelper = DatabaseHelper(this)
        containerMovements = findViewById(R.id.containerMovements)

        loadMovements()
    }

    private fun loadMovements() {

        containerMovements.removeAllViews()

        try {
            val movements = databaseHelper.getAllMovements()

            if (movements.isEmpty()) {
                val emptyMessage = TextView(this).apply {
                    text = "Todavía no hay movimientos registrados."
                    textSize = 16f
                    setPadding(24, 24, 24, 24)
                }

                containerMovements.addView(emptyMessage)
                return
            }

            val inflater = LayoutInflater.from(this)

            for (movement in movements) {

                val card = inflater.inflate(
                    R.layout.item_movement,
                    containerMovements,
                    false
                )

                val txtProduct =
                    card.findViewById<TextView>(R.id.txtMovementProduct)

                val txtType =
                    card.findViewById<TextView>(R.id.txtMovementType)

                val txtQuantity =
                    card.findViewById<TextView>(R.id.txtMovementQuantity)

                val txtCode =
                    card.findViewById<TextView>(R.id.txtMovementCode)

                val txtDate =
                    card.findViewById<TextView>(R.id.txtMovementDate)

                txtProduct.text = movement.productName
                txtCode.text = "Código: ${movement.productCode}"
                txtDate.text = "Fecha: ${movement.date}"

                if (movement.type == "ENTRADA") {

                    txtType.text = "ENTRADA"
                    txtType.setTextColor(Color.rgb(27, 125, 65))
                    txtType.setBackgroundColor(Color.rgb(220, 245, 228))

                    txtQuantity.text = "+${movement.quantity} unidades"
                    txtQuantity.setTextColor(Color.rgb(27, 125, 65))

                } else {

                    txtType.text = "SALIDA"
                    txtType.setTextColor(Color.rgb(190, 45, 45))
                    txtType.setBackgroundColor(Color.rgb(255, 225, 225))

                    txtQuantity.text = "-${movement.quantity} unidades"
                    txtQuantity.setTextColor(Color.rgb(190, 45, 45))
                }

                containerMovements.addView(card)
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar el historial",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}