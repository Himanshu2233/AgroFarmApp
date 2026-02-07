package com.example.agrofarm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrofarm.model.ProductModel
import com.example.agrofarm.repository.ProductRepo
import com.example.agrofarm.viewmodel.ProductViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ProductUnitTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val repo = mock<ProductRepo>()
    private val viewModel = ProductViewModel(repo)

    @Test
    fun addProduct_success_test() {
        // Create a dummy product based on your actual ProductModel
        val product = ProductModel(
            productId = "P101",
            name = "Organic Tomato",
            price = 2.5,
            quantity = 50
        )

        // Mocking repo.addProduct(product, callback)
        // Callback index is 1 (0: product, 1: callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Product added successfully")
            null
        }.`when`(repo).addProduct(eq(product), any())

        var successResult = false
        var messageResult = ""

        // Call the function
        viewModel.addProduct(product) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assertions
        assertTrue(successResult)
        assertEquals("Product added successfully", messageResult)
        assertEquals("Product added successfully", viewModel.message.value)
        verify(repo).addProduct(eq(product), any())
    }

    @Test
    fun getAllProducts_success_test() {
        val productList = listOf(
            ProductModel(productId = "1", name = "Apple"),
            ProductModel(productId = "2", name = "Banana")
        )

        // Mocking repo.getAllProducts(callback)
        // Callback index is 0 (it's the only argument)
        // Signature: (success: Boolean, message: String, data: List<ProductModel>?) -> Unit
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<ProductModel>?) -> Unit>(0)
            callback(true, "Fetched", productList)
            null
        }.`when`(repo).getAllProducts(any())

        viewModel.getAllProducts()

        // Verify LiveData updates
        assertEquals(2, viewModel.allProducts.value?.size)
        assertEquals("Apple", viewModel.allProducts.value?.get(0)?.name)
        verify(repo).getAllProducts(any())
    }

    @Test
    fun deleteProduct_success_test() {
        val productId = "P123"

        // Mocking repo.deleteProduct(productId, callback)
        // Callback index is 1 (0: productId, 1: callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Deleted")
            null
        }.`when`(repo).deleteProduct(eq(productId), any())

        var result = false
        viewModel.deleteProduct(productId) { success, _ ->
            result = success
        }

        assertTrue(result)
        verify(repo).deleteProduct(eq(productId), any())
    }
    @Test
    fun getProductById_success_test() {        val productId = "P101"
        val expectedProduct = ProductModel(
            productId = productId,
            name = "Organic Tomato",
            price = 2.5
        )

        // Mocking repo.getProductById(productId, callback)
        // Callback index is 1 (0: productId, 1: callback)
        // Signature: (success: Boolean, message: String, product: ProductModel?) -> Unit
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, ProductModel?) -> Unit>(1)
            callback(true, "Product found", expectedProduct)
            null
        }.`when`(repo).getProductById(eq(productId), any())

        viewModel.getProductById(productId)

        // Assertions
        assertEquals(expectedProduct, viewModel.product.value)
        assertEquals(productId, viewModel.product.value?.productId)
        verify(repo).getProductById(eq(productId), any())
    }

    @Test
    fun updateProduct_success_test() {
        val productId = "P101"
        val updatedProduct = ProductModel(
            productId = productId,
            name = "Premium Tomato",
            price = 3.0
        )

        // Mocking repo.updateProduct(productId, product, callback)
        // Callback index is 2 (0: id, 1: product, 2: callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Product updated successfully")
            null
        }.`when`(repo).updateProduct(eq(productId), eq(updatedProduct), any())

        var successResult = false
        var messageResult = ""

        viewModel.updateProduct(productId, updatedProduct) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assertions
        assertTrue(successResult)
        assertEquals("Product updated successfully", messageResult)
        assertEquals("Product updated successfully", viewModel.message.value)
        verify(repo).updateProduct(eq(productId), eq(updatedProduct), any())
    }
}