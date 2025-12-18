package com.example.agrofarm.repository

import com.example.agrofarm.model.ProductModel

interface ProductRepo {

    fun addProduct(
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    )

    fun getAllProducts(
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    )

    fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    )

    fun updateProduct(
        productId: String,
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    )
}