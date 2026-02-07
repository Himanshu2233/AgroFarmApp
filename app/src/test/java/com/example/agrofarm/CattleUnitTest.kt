package com.example.agrofarm


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrofarm.model.CattleModel
import com.example.agrofarm.repository.CattleRepo
import com.example.agrofarm.viewmodel.CattleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import android.content.Context
import android.net.Uri

class CattleUnitTest {

    // This rule ensures that LiveData updates happen instantly on the main thread
    @get:Rule
    val rule = InstantTaskExecutorRule()

    // Mock the repository dependency
    private val repo = mock<CattleRepo>()
    // Create the ViewModel with the mocked repository
    private val viewModel = CattleViewModel(repo)

    @Test
    fun addCattle_success_test() {
        val newCattle = CattleModel(
            id = "C001",
            farmerId = "F001",
            name = "MooMoo",
            type = "Cow",
            breed = "Jersey",
            age = 2,
            healthStatus = "Healthy",
            lastCheckup = "2023-01-15",
            imageUrl = "http://example.com/moomoo.jpg"
        )

        // Mock the repo.addCattle call to simulate success
        // Signature: addCattle(cattle: CattleModel, callback: (Boolean, String) -> Unit)
        // Callback is the second argument (index 1)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Cattle added successfully")
            null
        }.`when`(repo).addCattle(eq(newCattle), any())

        var successResult = false
        var messageResult = ""

        // Call the ViewModel function
        viewModel.addCattle(newCattle) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assertions
        assertTrue(successResult)
        assertEquals("Cattle added successfully", messageResult)
        // Verify LiveData updates
        assertEquals(false, viewModel.loading.value) // Loading should be false after completion
        assertEquals("Cattle added successfully", viewModel.message.value)
        // Verify that the repository method was called with the correct parameters
        verify(repo).addCattle(eq(newCattle), any())
    }

    @Test
    fun addCattle_failure_test() {
        val newCattle = CattleModel(id = "C002", name = "FailedCattle")

        // Mock the repo.addCattle call to simulate failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to add cattle")
            null
        }.`when`(repo).addCattle(eq(newCattle), any())

        var successResult = true // Start with true to check if it changes to false
        var messageResult = ""

        viewModel.addCattle(newCattle) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Failed to add cattle", messageResult)
        assertEquals(false, viewModel.loading.value)
        assertEquals("Failed to add cattle", viewModel.message.value)
        verify(repo).addCattle(eq(newCattle), any())
    }

    @Test
    fun getAllCattle_success_test() {
        val cattleList = listOf(
            CattleModel(id = "C001", name = "Bessie", age = 2),
            CattleModel(id = "C002", name = "Daisy", age = 3)
        )

        // Mock the repo.getAllCattle call to simulate success
        // Signature: getAllCattle(callback: (Boolean, String, List<CattleModel>?) -> Unit)
        // Callback is the first argument (index 0)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<CattleModel>?) -> Unit>(0)
            callback(true, "Cattle fetched successfully", cattleList)
            null
        }.`when`(repo).getAllCattle(any())

        viewModel.getAllCattle()

        // Assertions
        assertFalse(viewModel.loading.value ?: true) // Loading should be false
        assertEquals(2, viewModel.cattleList.value?.size)
        assertEquals("Bessie", viewModel.cattleList.value?.get(0)?.name)
        assertEquals("Daisy", viewModel.cattleList.value?.get(1)?.name)
        assertTrue(viewModel.message.value == null || viewModel.message.value == "") // Message might not be set on success
        verify(repo).getAllCattle(any())
    }

    @Test
    fun getAllCattle_failure_test() {
        // Mock the repo.getAllCattle call to simulate failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<CattleModel>?) -> Unit>(0)
            callback(false, "Failed to fetch cattle", null)
            null
        }.`when`(repo).getAllCattle(any())

        viewModel.getAllCattle()

        // Assertions
        assertFalse(viewModel.loading.value ?: true)
        assertTrue(viewModel.cattleList.value?.isEmpty() ?: false) // Should be empty list on failure
        assertEquals("Failed to fetch cattle", viewModel.message.value)
        verify(repo).getAllCattle(any())
    }

    @Test
    fun getCattleById_success_test() {
        val cattleId = "C001"
        val expectedCattle = CattleModel(id = cattleId, name = "Spot", type = "Bull")

        // Mock the repo.getCattleById call to simulate success
        // Signature: getCattleById(cattleId: String, callback: (Boolean, String, CattleModel?) -> Unit)
        // Callback is the second argument (index 1)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, CattleModel?) -> Unit>(1)
            callback(true, "Cattle found", expectedCattle)
            null
        }.`when`(repo).getCattleById(eq(cattleId), any())

        viewModel.getCattleById(cattleId)

        // Assertions
        assertFalse(viewModel.loading.value ?: true)
        assertEquals(expectedCattle, viewModel.cattle.value)
        assertEquals("Spot", viewModel.cattle.value?.name)
        verify(repo).getCattleById(eq(cattleId), any())
    }

    @Test
    fun getCattleById_failure_test() {
        val cattleId = "C001"

        // Mock the repo.getCattleById call to simulate failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, CattleModel?) -> Unit>(1)
            callback(false, "Cattle not found", null)
            null
        }.`when`(repo).getCattleById(eq(cattleId), any())

        viewModel.getCattleById(cattleId)

        // Assertions
        assertFalse(viewModel.loading.value ?: true)
        assertNull(viewModel.cattle.value) // Should be null on failure
        assertEquals("Cattle not found", viewModel.message.value)
        verify(repo).getCattleById(eq(cattleId), any())
    }

    @Test
    fun updateCattle_success_test() {
        val cattleId = "C001"
        val updatedCattle = CattleModel(id = cattleId, name = "MooMoo V2", age = 3)

        // Mock the repo.updateCattle call to simulate success
        // Signature: updateCattle(cattleId: String, cattle: CattleModel, callback: (Boolean, String) -> Unit)
        // Callback is the third argument (index 2)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Cattle updated successfully")
            null
        }.`when`(repo).updateCattle(eq(cattleId), eq(updatedCattle), any())

        var successResult = false
        var messageResult = ""

        viewModel.updateCattle(cattleId, updatedCattle) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assertions
        assertTrue(successResult)
        assertEquals("Cattle updated successfully", messageResult)
        assertFalse(viewModel.loading.value ?: true)
        assertEquals("Cattle updated successfully", viewModel.message.value)
        verify(repo).updateCattle(eq(cattleId), eq(updatedCattle), any())
    }

    @Test
    fun deleteCattle_success_test() {
        val cattleId = "C001"

        // Mock the repo.deleteCattle call to simulate success
        // Signature: deleteCattle(cattleId: String, callback: (Boolean, String) -> Unit)
        // Callback is the second argument (index 1)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Cattle deleted successfully")
            null
        }.`when`(repo).deleteCattle(eq(cattleId), any())

        var successResult = false
        var messageResult = ""

        viewModel.deleteCattle(cattleId) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assertions
        assertTrue(successResult)
        assertEquals("Cattle deleted successfully", messageResult)
        assertFalse(viewModel.loading.value ?: true)
        assertEquals("Cattle deleted successfully", viewModel.message.value)
        verify(repo).deleteCattle(eq(cattleId), any())
    }

    @Test
    fun uploadCattleImage_success_test() {
        val mockContext = mock<Context>()
        val mockUri = mock<Uri>()
        val expectedImageUrl = "https://cloudinary.com/new_image.jpg"

        // Mock the repo.uploadCattleImage call to simulate success
        // Signature: uploadCattleImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
        // Callback is the third argument (index 2)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String?) -> Unit>(2)
            callback(expectedImageUrl)
            null
        }.`when`(repo).uploadCattleImage(eq(mockContext), eq(mockUri), any())

        var imageUrlResult: String? = null
        viewModel.uploadCattleImage(mockContext, mockUri) { url ->
            imageUrlResult = url
        }

        // Assertions
        assertNotNull(imageUrlResult)
        assertEquals(expectedImageUrl, imageUrlResult)
        // Note: _loading and _message LiveData are not directly affected by uploadCattleImage in ViewModel
        // as per your current implementation.
        verify(repo).uploadCattleImage(eq(mockContext), eq(mockUri), any())
    }

    @Test
    fun uploadCattleImage_failure_test() {
        val mockContext = mock<Context>()
        val mockUri = mock<Uri>()

        // Mock the repo.uploadCattleImage call to simulate failure (null URL)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String?) -> Unit>(2)
            callback(null)
            null
        }.`when`(repo).uploadCattleImage(eq(mockContext), eq(mockUri), any())

        var imageUrlResult: String? = "initialValue" // Should become null
        viewModel.uploadCattleImage(mockContext, mockUri) { url ->
            imageUrlResult = url
        }

        // Assertions
        assertNull(imageUrlResult)
        verify(repo).uploadCattleImage(eq(mockContext), eq(mockUri), any())
    }
}