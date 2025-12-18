package com.example.agrofarm.model

data class UserModel(
    val userId:String = "",
    val email:String = "",
    val password:String = "",
    val fullName:String = "",
    val dob:String = "",

) {

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "password" to password,
            "fullName" to fullName,
            "dob" to dob,
            "role" to "user"
        )
    }
}
