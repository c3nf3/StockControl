package cl.stockcontrol.app.models

data class Movement(
    val id: Int,
    val productName: String,
    val productCode: String,
    val type: String,
    val quantity: Int,
    val date: String
)