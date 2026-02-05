package com.example.agrofarm.model

data class UserModel(
    val userId: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val dob: String = "",
    // New fields for farmer profile
    val gender: String = "",
    val phone: String = "",
    val farmName: String = "",
    val farmSize: String = "",          // e.g., "50 acres"
    val farmAddress: String = "",
    val farmingExperience: Int = 0,     // years of experience
    val specialization: String = "",     // e.g., "Dairy Farming", "Organic Crops"
    val profileImageUrl: String = "",
    val role: String = "farmer"
) {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "password" to password,
            "fullName" to fullName,
            "dob" to dob,
            "gender" to gender,
            "phone" to phone,
            "farmName" to farmName,
            "farmSize" to farmSize,
            "farmAddress" to farmAddress,
            "farmingExperience" to farmingExperience,
            "specialization" to specialization,
            "profileImageUrl" to profileImageUrl,
            "role" to role
        )
    }
}
