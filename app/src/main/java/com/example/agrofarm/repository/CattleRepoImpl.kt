package com.example.agrofarm.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.agrofarm.model.CattleModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class CattleRepoImpl : CattleRepo {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("cattle")
    private val cloudinary = Cloudinary(mapOf(
        "cloud_name" to "dlpal9dve",
        "api_key" to "358118928588627",
        "api_secret" to "JQdwGpMaLIOYrrp8kOwJd7X6Ql0"
    ))

    override fun addCattle(
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    ) {
        val cattleId = UUID.randomUUID().toString()
        val newCattle = cattle.copy(id = cattleId)
        
        database.child(cattleId).setValue(newCattle.toMap())
            .addOnSuccessListener {
                callback(true, "Cattle added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to add cattle: ${e.message}")
            }
    }

    override fun getAllCattle(callback: (Boolean, String, List<CattleModel>?) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No cattle found", emptyList())
                    return
                }
                
                val cattleList = mutableListOf<CattleModel>()
                try {
                    for (cattleSnapshot in snapshot.children) {
                        val cattle = cattleSnapshot.getValue(CattleModel::class.java)
                        if (cattle != null) {
                            cattleList.add(cattle)
                        }
                    }
                    callback(true, "Cattle loaded successfully", cattleList)
                } catch (e: Exception) {
                    callback(false, "Error loading cattle: ${e.message}", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", emptyList())
            }
        })
    }

    override fun getCattleById(
        cattleId: String,
        callback: (Boolean, String, CattleModel?) -> Unit
    ) {
        database.child(cattleId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "Cattle not found", null)
                    return
                }
                
                try {
                    val cattle = snapshot.getValue(CattleModel::class.java)
                    if (cattle != null) {
                        callback(true, "Cattle found", cattle)
                    } else {
                        callback(false, "Error parsing cattle data", null)
                    }
                } catch (e: Exception) {
                    callback(false, "Error: ${e.message}", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    override fun updateCattle(
        cattleId: String,
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(cattleId).setValue(cattle.toMap())
            .addOnSuccessListener {
                callback(true, "Cattle updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update: ${e.message}")
            }
    }

    override fun deleteCattle(
        cattleId: String,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(cattleId).removeValue()
            .addOnSuccessListener {
                callback(true, "Cattle deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete: ${e.message}")
            }
    }

    // ✅ ADDED: Implementation for cattle image upload
    override fun uploadCattleImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val fileName = getFileNameFromUri(context, imageUri)?.substringBeforeLast(".") ?: UUID.randomUUID().toString()

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", "cattle_$fileName", // Prefix to organize in Cloudinary
                        "resource_type", "image"
                    )
                )

                // Use secure_url which is already HTTPS
                val imageUrl = response["secure_url"] as String?
                Handler(Looper.getMainLooper()).post { callback(imageUrl) }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post { callback(null) }
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}