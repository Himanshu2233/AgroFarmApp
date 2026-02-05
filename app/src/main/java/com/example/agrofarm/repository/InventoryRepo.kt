package com.example.agrofarm.repository

import android.content.Context
import android.net.Uri
import com.example.agrofarm.model.InventoryModel

interface InventoryRepo {
    fun addInventoryItem(
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllInventoryItems(callback: (Boolean, String, List<InventoryModel>?) -> Unit)

    fun getInventoryItemById(
        itemId: String,
        callback: (Boolean, String, InventoryModel?) -> Unit
    )

    fun updateInventoryItem(
        itemId: String,
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteInventoryItem(
        itemId: String,
        callback: (Boolean, String) -> Unit
    )

    fun uploadInventoryImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )
}
