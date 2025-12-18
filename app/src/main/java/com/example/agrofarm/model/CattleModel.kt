package com.example.agrofarm.model

data class CattleModel(
    val id: String = "",
    val farmerId: String = "",
    val name: String = "",
    val type: String = "",
    val breed: String = "",
    val age: Int = 0,
    val healthStatus: String = "",
    val lastCheckup: String = ""
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
            "lastCheckup" to lastCheckup
        )
    }
}