
package cl.stockcontrol.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cl.stockcontrol.app.database.DatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var txtTotalProducts: TextView
    private lateinit var txtLowStock: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)

        txtTotalProducts = findViewById(R.id.txtTotalProducts)
        txtLowStock = findViewById(R.id.txtLowStock)

        val btnProducts = findViewById<Button>(R.id.btnProducts)
        val btnAddProduct = findViewById<Button>(R.id.btnAddProduct)
        val btnMovements = findViewById<Button>(R.id.btnMovements)
        val btnMovementHistory =
            findViewById<Button>(R.id.btnMovementHistory)
        val btnLowStock = findViewById<Button>(R.id.btnLowStock)

        btnLowStock.setOnClickListener {
            startActivity(
                Intent(this, LowStockActivity::class.java)
            )
        }

        btnMovementHistory.setOnClickListener {
            startActivity(
                Intent(this, MovementHistoryActivity::class.java)
            )
        }

        btnMovements.setOnClickListener {
            startActivity(
                Intent(this, MovementActivity::class.java)
            )
        }

        btnProducts.setOnClickListener {
            // La lista de productos la implementaremos
            // en la siguiente etapa.
            startActivity(
                Intent(this, ProductListActivity::class.java)
            )
        }

        btnAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if (::databaseHelper.isInitialized) {
            updateDashboard()
        }
    }

    private fun updateDashboard() {

        val total = databaseHelper.getProductCount()
        val lowStock = databaseHelper.getLowStockCount()

        txtTotalProducts.text = total.toString()
        txtLowStock.text = lowStock.toString()
    }

    override fun onDestroy() {
        if (::databaseHelper.isInitialized) {
            databaseHelper.close()
        }
        super.onDestroy()
    }
}
