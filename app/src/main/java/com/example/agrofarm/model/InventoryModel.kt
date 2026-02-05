package com.example.agrofarm.model

data class InventoryModel(
    val id: String = "",
    val farmerId: String = "",
    val name: String = "",
    val category: String = "",          // Tools, Equipment, Seeds, Fertilizers, Pesticides, Animal Feed, Other
    val description: String = "",
    val quantity: Int = 0,
    val unit: String = "",              // pieces, bags, liters, kg, etc.
    val purchaseDate: String = "",
    val purchasePrice: Double = 0.0,
    val condition: String = "",         // New, Good, Fair, Poor, Needs Repair
    val location: String = "",          // Where stored: Warehouse, Shed, Field, etc.
    val imageUrl: String = "",
    val lastMaintenanceDate: String = "",
    val nextMaintenanceDate: String = "",
    val supplier: String = "",
    val warrantyExpiry: String = "",
    val notes: String = "",
    val isActive: Boolean = true        // Whether item is still in use
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "farmerId" to farmerId,
            "name" to name,
            "category" to category,
            "description" to description,
            "quantity" to quantity,
            "unit" to unit,
            "purchaseDate" to purchaseDate,
            "purchasePrice" to purchasePrice,
            "condition" to condition,
            "location" to location,
            "imageUrl" to imageUrl,
            "lastMaintenanceDate" to lastMaintenanceDate,
            "nextMaintenanceDate" to nextMaintenanceDate,
            "supplier" to supplier,
            "warrantyExpiry" to warrantyExpiry,
            "notes" to notes,
            "isActive" to isActive
        )
    }
}
