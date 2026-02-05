package com.example.agrofarm.model

data class CattleModel(
    val id: String = "",
    val farmerId: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val healthStatus: String = "",
    val lastCheckup: String = "",
    val imageUrl: String = "",
    // New fields for comprehensive cattle management
    val gender: String = "",
    val weight: Double = 0.0,           // in kg
    val purchaseDate: String = "",
    val purchasePrice: Double = 0.0,
    val tagNumber: String = "",          // ear tag or identification number
    val vaccinationStatus: String = "",
    val lastVaccination: String = "",
    val milkProduction: Double = 0.0,    // daily milk production in liters (for dairy animals)
    val feedType: String = "",
    val notes: String = "",
    val isPregnant: Boolean = false,
    val expectedDelivery: String = "",
    val parentId: String = ""            // for tracking lineage
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "farmerId" to farmerId,
            "name" to name,
            "type" to type,
            "breed" to breed,
            "age" to age,
            "healthStatus" to healthStatus,
            "lastCheckup" to lastCheckup,
            "imageUrl" to imageUrl,
            "gender" to gender,
            "weight" to weight,
            "purchaseDate" to purchaseDate,
            "purchasePrice" to purchasePrice,
            "tagNumber" to tagNumber,
            "vaccinationStatus" to vaccinationStatus,
            "lastVaccination" to lastVaccination,
            "milkProduction" to milkProduction,
            "feedType" to feedType,
            "notes" to notes,
            "isPregnant" to isPregnant,
            "expectedDelivery" to expectedDelivery,
            "parentId" to parentId
        )
    }
}