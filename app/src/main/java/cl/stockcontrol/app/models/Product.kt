
package cl.stockcontrol.app.models

data class Product(
    val id: Int = 0,
    val code: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val minStock: Int
)
