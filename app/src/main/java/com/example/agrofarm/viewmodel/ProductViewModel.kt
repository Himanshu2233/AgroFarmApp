package com.example.agrofarm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepo

class ProductViewModel(private val repo: ProductRepo) : ViewModel() {

    private val _product = MutableLiveData<ProductModel?>()
    val product: LiveData<ProductModel?> get() = _product

    private val _allProducts = MutableLiveData<List<ProductModel>>(emptyList())
    val allProducts: LiveData<List<ProductModel>> get() = _allProducts

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun addProduct(
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.addProduct(product) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun getAllProducts() {
        _loading.postValue(true)
        repo.getAllProducts { success, message, data ->
            _loading.postValue(false)
            _message.postValue(message)
            _allProducts.postValue(data ?: emptyList())
        }
    }

    fun getProductById(productId: String) {
        _loading.postValue(true)
        repo.getProductById(productId) { success, msg, product ->
            _loading.postValue(false)
            if (success && product != null) {
                _product.postValue(product)
            } else {
                _product.postValue(null)
                _message.postValue(msg)
            }
        }
    }

    fun updateProduct(
        productId: String,
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.updateProduct(productId, product) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }

    fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        repo.deleteProduct(productId) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            callback(success, msg)
        }
    }
}