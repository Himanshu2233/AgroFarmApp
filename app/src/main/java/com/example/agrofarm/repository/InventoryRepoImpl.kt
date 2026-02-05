package com.example.agrofarm.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.agrofarm.model.InventoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class InventoryRepoImpl : InventoryRepo {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("inventory")
    private val cloudinary = Cloudinary(mapOf(
        "cloud_name" to "dlpal9dve",
        "api_key" to "358118928588627",
        "api_secret" to "JQdwGpMaLIOYrrp8kOwJd7X6Ql0"
    ))

    override fun addInventoryItem(
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    ) {
        val itemId = UUID.randomUUID().toString()
        val newItem = item.copy(id = itemId)
        
        database.child(itemId).setValue(newItem.toMap())
            .addOnSuccessListener {
                callback(true, "Inventory item added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to add item: ${e.message}")
            }
    }

    override fun getAllInventoryItems(callback: (Boolean, String, List<InventoryModel>?) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No inventory items found", emptyList())
                    return
                }
                
                val itemList = mutableListOf<InventoryModel>()
                try {
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(InventoryModel::class.java)
                        if (item != null) {
                            itemList.add(item)
                        }
                    }
                    callback(true, "Inventory loaded successfully", itemList)
                } catch (e: Exception) {
                    callback(false, "Error loading inventory: ${e.message}", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", emptyList())
            }
        })
    }

    override fun getInventoryItemById(
        itemId: String,
        callback: (Boolean, String, InventoryModel?) -> Unit
    ) {
        database.child(itemId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "Item not found", null)
                    return
                }
                
                try {
                    val item = snapshot.getValue(InventoryModel::class.java)
                    if (item != null) {
                        callback(true, "Item found", item)
                    } else {
                        callback(false, "Error parsing item data", null)
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

    override fun updateInventoryItem(
        itemId: String,
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(itemId).setValue(item.toMap())
            .addOnSuccessListener {
                callback(true, "Item updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update: ${e.message}")
            }
    }

    override fun deleteInventoryItem(
        itemId: String,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(itemId).removeValue()
            .addOnSuccessListener {
                callback(true, "Item deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete: ${e.message}")
            }
    }

    override fun uploadInventoryImage(
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
                        "public_id", "inventory_$fileName",
                        "resource_type", "image"
                    )
                )

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
