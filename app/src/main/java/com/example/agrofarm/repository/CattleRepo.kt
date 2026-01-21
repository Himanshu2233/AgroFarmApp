package com.example.agrofarm.repository

import android.content.Context
import android.net.Uri
import com.example.agrofarm.model.CattleModel

interface CattleRepo {
    fun addCattle(
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllCattle(callback: (Boolean, String, List<CattleModel>?) -> Unit)

    fun getCattleById(
        cattleId: String,
        callback: (Boolean, String, CattleModel?) -> Unit
    )

    fun updateCattle(
        cattleId: String,
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteCattle(
        cattleId: String,
        callback: (Boolean, String) -> Unit
    )

    // ✅ FIXED: Ensures the interface has the function for Cloudinary uploads
    fun uploadCattleImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )
}