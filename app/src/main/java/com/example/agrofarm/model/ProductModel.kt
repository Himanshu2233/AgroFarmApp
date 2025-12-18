package com.example.agrofarm.model

data class ProductModel(
    val productId: String = "",
    val farmerId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 0,
    val category: String = "",
    val unit: String = "",
    val harvestDate: Long = 0L,
    val isOrganic: Boolean = false,
    val location: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "productId" to productId,
            "farmerId" to farmerId,
            "name" to name,
            "description" to description,
            "price" to price,
            "imageUrl" to imageUrl,
            "quantity" to quantity,
            "category" to category,
            "unit" to unit,
            "harvestDate" to harvestDate,
            "isOrganic" to isOrganic,
            "location" to location
        )
    }
}