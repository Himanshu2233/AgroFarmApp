package com.example.agrofarm.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.agrofarm.model.InventoryModel
import com.example.agrofarm.repository.InventoryRepo

class InventoryViewModel(private val repo: InventoryRepo) : ViewModel() {

    private val _inventoryList = MutableLiveData<List<InventoryModel>>(emptyList())
    val inventoryList: LiveData<List<InventoryModel>> get() = _inventoryList

    private val _inventoryItem = MutableLiveData<InventoryModel?>()
    val inventoryItem: LiveData<InventoryModel?> get() = _inventoryItem

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun addInventoryItem(
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.addInventoryItem(item) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun getAllInventoryItems() {
        _loading.postValue(true)
        repo.getAllInventoryItems { success, msg, data ->
            _loading.postValue(false)
            _inventoryList.postValue(data ?: emptyList())
            if(!success) _message.postValue(msg)
        }
    }

    fun getInventoryItemById(itemId: String) {
        _loading.postValue(true)
        repo.getInventoryItemById(itemId) { success, msg, itemData ->
            _loading.postValue(false)
            if (success) {
                _inventoryItem.postValue(itemData)
            } else {
                _message.postValue(msg)
                _inventoryItem.postValue(null)
            }
        }
    }

    fun updateInventoryItem(
        itemId: String,
        item: InventoryModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.updateInventoryItem(itemId, item) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun deleteInventoryItem(
        itemId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.deleteInventoryItem(itemId) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun uploadInventoryImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.uploadInventoryImage(context, imageUri, callback)
    }
}
