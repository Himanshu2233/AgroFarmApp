package com.example.agrofarm.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepo

class CattleViewModel(private val repo: CattleRepo) : ViewModel() {

    // ✅ FIXED: Initialized with an empty list to make it non-nullable
    private val _cattleList = MutableLiveData<List<CattleModel>>(emptyList())
    val cattleList: LiveData<List<CattleModel>> get() = _cattleList

    private val _cattle = MutableLiveData<CattleModel?>()
    val cattle: LiveData<CattleModel?> get() = _cattle

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun addCattle(
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.addCattle(cattle) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun getAllCattle() {
        _loading.postValue(true)
        repo.getAllCattle { success, msg, data ->
            _loading.postValue(false)
            // ✅ FIXED: Provide a default empty list if data is null
            _cattleList.postValue(data ?: emptyList())
            if(!success) _message.postValue(msg)
        }
    }

    fun getCattleById(cattleId: String) {
        _loading.postValue(true)
        repo.getCattleById(cattleId) { success, msg, cattleData ->
            _loading.postValue(false)
            if (success) {
                _cattle.postValue(cattleData)
            } else {
                _message.postValue(msg)
                _cattle.postValue(null)
            }
        }
    }

    fun updateCattle(
        cattleId: String,
        cattle: CattleModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.updateCattle(cattleId, cattle) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun deleteCattle(
        cattleId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.deleteCattle(cattleId) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun uploadCattleImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        repo.uploadCattleImage(context, imageUri, callback)
    }
}