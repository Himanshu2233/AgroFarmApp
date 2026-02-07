package com.example.agrofarm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.agrofarm.model.InventoryModel
import com.example.agrofarm.repository.InventoryRepo
import com.example.agrofarm.viewmodel.InventoryViewModel
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

class InventoryUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val repo = mock<InventoryRepo>()
    private val viewModel = InventoryViewModel(repo)

    // ==================== ADD INVENTORY TESTS ====================

    @Test
    fun addInventoryItem_success_test() {
        val newItem = InventoryModel(
            id = "INV001",
            farmerId = "F001",
            name = "Tractor",
            category = "Equipment",
            description = "John Deere Tractor",
            quantity = 1,
            unit = "pieces",
            purchaseDate = "2024-01-15",
            purchasePrice = 50000.0,
            condition = "New",
            location = "Barn",
            isActive = true
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Inventory item added successfully")
            null
        }.`when`(repo).addInventoryItem(eq(newItem), any())

        var successResult = false
        var messageResult = ""

        viewModel.addInventoryItem(newItem) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Inventory item added successfully", messageResult)
        assertEquals(false, viewModel.loading.value)
        verify(repo).addInventoryItem(eq(newItem), any())
    }

    @Test
    fun addInventoryItem_failure_test() {
        val newItem = InventoryModel(id = "INV002", name = "Failed Item")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to add inventory item")
            null
        }.`when`(repo).addInventoryItem(eq(newItem), any())

        var successResult = true
        var messageResult = ""

        viewModel.addInventoryItem(newItem) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Failed to add inventory item", messageResult)
        verify(repo).addInventoryItem(eq(newItem), any())
    }

    // ==================== GET ALL INVENTORY TESTS ====================

    @Test
    fun getAllInventoryItems_success_test() {
        val inventoryList = listOf(
            InventoryModel(id = "INV001", name = "Tractor", category = "Equipment"),
            InventoryModel(id = "INV002", name = "Seeds", category = "Seeds"),
            InventoryModel(id = "INV003", name = "Fertilizer", category = "Fertilizers")
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<InventoryModel>?) -> Unit>(0)
            callback(true, "Items loaded", inventoryList)
            null
        }.`when`(repo).getAllInventoryItems(any())

        viewModel.getAllInventoryItems()

        assertEquals(inventoryList, viewModel.inventoryList.value)
        assertEquals(false, viewModel.loading.value)
        verify(repo).getAllInventoryItems(any())
    }

    @Test
    fun getAllInventoryItems_empty_test() {
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<InventoryModel>?) -> Unit>(0)
            callback(true, "No items found", emptyList())
            null
        }.`when`(repo).getAllInventoryItems(any())

        viewModel.getAllInventoryItems()

        assertTrue(viewModel.inventoryList.value?.isEmpty() == true)
        verify(repo).getAllInventoryItems(any())
    }

    @Test
    fun getAllInventoryItems_failure_test() {
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<InventoryModel>?) -> Unit>(0)
            callback(false, "Database error", null)
            null
        }.`when`(repo).getAllInventoryItems(any())

        viewModel.getAllInventoryItems()

        assertEquals("Database error", viewModel.message.value)
        verify(repo).getAllInventoryItems(any())
    }

    // ==================== GET INVENTORY BY ID TESTS ====================

    @Test
    fun getInventoryItemById_success_test() {
        val item = InventoryModel(
            id = "INV001",
            name = "Tractor",
            category = "Equipment",
            quantity = 1
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, InventoryModel?) -> Unit>(1)
            callback(true, "Item found", item)
            null
        }.`when`(repo).getInventoryItemById(eq("INV001"), any())

        viewModel.getInventoryItemById("INV001")

        assertEquals(item, viewModel.inventoryItem.value)
        assertEquals(false, viewModel.loading.value)
        verify(repo).getInventoryItemById(eq("INV001"), any())
    }

    @Test
    fun getInventoryItemById_notFound_test() {
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, InventoryModel?) -> Unit>(1)
            callback(false, "Item not found", null)
            null
        }.`when`(repo).getInventoryItemById(eq("INVALID"), any())

        viewModel.getInventoryItemById("INVALID")

        assertNull(viewModel.inventoryItem.value)
        assertEquals("Item not found", viewModel.message.value)
        verify(repo).getInventoryItemById(eq("INVALID"), any())
    }

    // ==================== UPDATE INVENTORY TESTS ====================

    @Test
    fun updateInventoryItem_success_test() {
        val updatedItem = InventoryModel(
            id = "INV001",
            name = "Updated Tractor",
            quantity = 2
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Inventory item updated successfully")
            null
        }.`when`(repo).updateInventoryItem(eq("INV001"), eq(updatedItem), any())

        var successResult = false
        var messageResult = ""

        viewModel.updateInventoryItem("INV001", updatedItem) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Inventory item updated successfully", messageResult)
        verify(repo).updateInventoryItem(eq("INV001"), eq(updatedItem), any())
    }

    @Test
    fun updateInventoryItem_failure_test() {
        val updatedItem = InventoryModel(id = "INV001", name = "Failed Update")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Failed to update item")
            null
        }.`when`(repo).updateInventoryItem(eq("INV001"), eq(updatedItem), any())

        var successResult = true
        var messageResult = ""

        viewModel.updateInventoryItem("INV001", updatedItem) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Failed to update item", messageResult)
        verify(repo).updateInventoryItem(eq("INV001"), eq(updatedItem), any())
    }

    // ==================== DELETE INVENTORY TESTS ====================

    @Test
    fun deleteInventoryItem_success_test() {
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Inventory item deleted successfully")
            null
        }.`when`(repo).deleteInventoryItem(eq("INV001"), any())

        var successResult = false
        var messageResult = ""

        viewModel.deleteInventoryItem("INV001") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Inventory item deleted successfully", messageResult)
        verify(repo).deleteInventoryItem(eq("INV001"), any())
    }

    @Test
    fun deleteInventoryItem_failure_test() {
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to delete item")
            null
        }.`when`(repo).deleteInventoryItem(eq("INV001"), any())

        var successResult = true
        var messageResult = ""

        viewModel.deleteInventoryItem("INV001") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Failed to delete item", messageResult)
        verify(repo).deleteInventoryItem(eq("INV001"), any())
    }

    // ==================== INVENTORY MODEL TESTS ====================

    @Test
    fun inventoryModel_toMap_test() {
        val item = InventoryModel(
            id = "INV001",
            farmerId = "F001",
            name = "Tractor",
            category = "Equipment",
            description = "Farm tractor",
            quantity = 1,
            unit = "pieces",
            purchaseDate = "2024-01-15",
            purchasePrice = 50000.0,
            condition = "New",
            location = "Barn",
            isActive = true
        )

        val map = item.toMap()

        assertEquals("INV001", map["id"])
        assertEquals("F001", map["farmerId"])
        assertEquals("Tractor", map["name"])
        assertEquals("Equipment", map["category"])
        assertEquals(1, map["quantity"])
        assertEquals(50000.0, map["purchasePrice"])
        assertEquals(true, map["isActive"])
    }

    @Test
    fun inventoryModel_defaultValues_test() {
        val item = InventoryModel()

        assertEquals("", item.id)
        assertEquals("", item.farmerId)
        assertEquals("", item.name)
        assertEquals(0, item.quantity)
        assertEquals(0.0, item.purchasePrice, 0.001)
        assertEquals(true, item.isActive)
    }
}
