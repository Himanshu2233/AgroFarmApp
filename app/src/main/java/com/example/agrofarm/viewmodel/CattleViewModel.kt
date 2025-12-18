package com.example.agrofarm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepo

class CattleViewModel(private val repo: CattleRepo) : ViewModel() {

    private val _cattleList = MutableLiveData<List<CattleModel>?>()
    val cattleList: MutableLiveData<List<CattleModel>?> get() = _cattleList

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String>()
    val message: MutableLiveData<String> get() = _message

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
            _cattleList.postValue(data)
            _message.postValue(msg)
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

    companion object {
        fun Factory(repo: CattleRepo): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CattleViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return CattleViewModel(repo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}