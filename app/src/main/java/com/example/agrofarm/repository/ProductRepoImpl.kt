package com.example.agrofarm.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.agrofarm.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class ProductRepoImpl : ProductRepo {

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dlpal9dve",
            "api_key" to "358118928588627",
            "api_secret" to "JQdwGpMaLIOYrrp8kOwJd7X6Ql0"
        )
    )
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("products")

    override fun addProduct(
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val productId = UUID.randomUUID().toString()
        val newProduct = product.copy(productId = productId)

        database.child(productId).setValue(newProduct.toMap())
            .addOnSuccessListener {
                callback(true, "Product added successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to add product: ${e.message}")
            }
    }

    override fun getAllProducts(
        callback: (Boolean, String, List<ProductModel>?) -> Unit
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(true, "No products found", emptyList())
                    return
                }

                val products = mutableListOf<ProductModel>()
                try {
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductModel::class.java)
                        if (product != null) {
                            products.add(product)
                        }
                    }
                    callback(true, "Products loaded successfully", products)
                } catch (e: Exception) {
                    callback(false, "Error loading products: ${e.message}", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", emptyList())
            }
        })
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        database.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "Product not found", null)
                    return
                }

                try {
                    val product = snapshot.getValue(ProductModel::class.java)
                    if (product != null) {
                        callback(true, "Product found", product)
                    } else {
                        callback(false, "Error parsing product data", null)
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

    override fun updateProduct(
        productId: String,
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        val updatedProduct = product.copy(productId = productId)

        database.child(productId).setValue(updatedProduct.toMap())
            .addOnSuccessListener {
                callback(true, "Product updated successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to update: ${e.message}")
            }
    }

    override fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        database.child(productId).removeValue()
            .addOnSuccessListener {
                callback(true, "Product deleted successfully")
            }
            .addOnFailureListener { e ->
                callback(false, "Failed to delete: ${e.message}")
            }
    }

    override fun uploadProductImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    ) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                // Use secure_url which is already HTTPS
                val imageUrl = response["secure_url"] as String?

                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(
        context: Context,
        uri: Uri
    ): String? {
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