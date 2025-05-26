package com.example.supplyline_mro_suite.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.model.Chemical
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ChemicalDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: SupplyLineDatabase
    private lateinit var chemicalDao: ChemicalDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java
        ).allowMainThreadQueries().build()

        chemicalDao = database.chemicalDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertChemical_and_getChemicalById() = runTest {
        // Given
        val chemical = Chemical(
            id = 1,
            partNumber = "PN001",
            lotNumber = "LOT001",
            description = "Test Chemical",
            manufacturer = "Test Manufacturer",
            category = "Sealant",
            location = "A1",
            quantity = 100.0,
            unit = "ml",
            expirationDate = "2025-12-31",
            minimumStockLevel = 10.0,
            status = "Good"
        )

        // When
        chemicalDao.insertChemical(chemical)
        val retrievedChemical = chemicalDao.getChemicalById(1)

        // Then
        assertNotNull(retrievedChemical)
        assertEquals(chemical.partNumber, retrievedChemical?.partNumber)
        assertEquals(chemical.lotNumber, retrievedChemical?.lotNumber)
        assertEquals(chemical.description, retrievedChemical?.description)
    }

    @Test
    fun getChemicalByPartAndLot() = runTest {
        // Given
        val chemical = Chemical(
            id = 1,
            partNumber = "PN001",
            lotNumber = "LOT001",
            description = "Test Chemical",
            manufacturer = "Test Manufacturer",
            category = "Sealant",
            location = "A1",
            quantity = 100.0,
            unit = "ml",
            expirationDate = "2025-12-31",
            minimumStockLevel = 10.0,
            status = "Good"
        )

        // When
        chemicalDao.insertChemical(chemical)
        val retrievedChemical = chemicalDao.getChemicalByPartAndLot("PN001", "LOT001")

        // Then
        assertNotNull(retrievedChemical)
        assertEquals(chemical.id, retrievedChemical?.id)
        assertEquals(chemical.description, retrievedChemical?.description)
    }

    @Test
    fun getAllActiveChemicals() = runTest {
        // Given
        val chemicals = listOf(
            Chemical(1, "PN001", "LOT001", "Chemical 1", "Manufacturer", "Sealant", "A1",
                    100.0, "ml", "2025-12-31", 10.0, "Good", isArchived = false),
            Chemical(2, "PN002", "LOT002", "Chemical 2", "Manufacturer", "Paint", "A2",
                    200.0, "ml", "2025-12-31", 20.0, "Good", isArchived = false),
            Chemical(3, "PN003", "LOT003", "Chemical 3", "Manufacturer", "Adhesive", "A3",
                    150.0, "ml", "2025-12-31", 15.0, "Good", isArchived = true)
        )

        // When
        chemicalDao.insertChemicals(chemicals)
        val activeChemicals = chemicalDao.getAllActiveChemicals().first()

        // Then
        assertEquals(2, activeChemicals.size)
        assertTrue(activeChemicals.all { !it.isArchived })
    }

    @Test
    fun getChemicalsByCategory() = runTest {
        // Given
        val chemicals = listOf(
            Chemical(1, "PN001", "LOT001", "Chemical 1", "Manufacturer", "Sealant", "A1",
                    100.0, "ml", "2025-12-31", 10.0, "Good"),
            Chemical(2, "PN002", "LOT002", "Chemical 2", "Manufacturer", "Sealant", "A2",
                    200.0, "ml", "2025-12-31", 20.0, "Good"),
            Chemical(3, "PN003", "LOT003", "Chemical 3", "Manufacturer", "Paint", "A3",
                    150.0, "ml", "2025-12-31", 15.0, "Good")
        )

        // When
        chemicalDao.insertChemicals(chemicals)
        val sealantChemicals = chemicalDao.getChemicalsByCategory("Sealant").first()

        // Then
        assertEquals(2, sealantChemicals.size)
        assertTrue(sealantChemicals.all { it.category == "Sealant" })
    }

    @Test
    fun searchChemicals() = runTest {
        // Given
        val chemicals = listOf(
            Chemical(1, "PN001", "LOT001", "Epoxy Sealant", "Manufacturer", "Sealant", "A1",
                    100.0, "ml", "2025-12-31", 10.0, "Good"),
            Chemical(2, "PN002", "LOT002", "Acrylic Paint", "Manufacturer", "Paint", "A2",
                    200.0, "ml", "2025-12-31", 20.0, "Good"),
            Chemical(3, "PN003", "LOT003", "Epoxy Adhesive", "Manufacturer", "Adhesive", "A3",
                    150.0, "ml", "2025-12-31", 15.0, "Good")
        )

        // When
        chemicalDao.insertChemicals(chemicals)
        val searchResults = chemicalDao.searchChemicals("Epoxy").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.description.contains("Epoxy") })
    }

    @Test
    fun getLowStockChemicals() = runTest {
        // Given
        val chemicals = listOf(
            Chemical(1, "PN001", "LOT001", "Chemical 1", "Manufacturer", "Sealant", "A1",
                    5.0, "ml", "2025-12-31", 10.0, "Low Stock"), // Below minimum
            Chemical(2, "PN002", "LOT002", "Chemical 2", "Manufacturer", "Paint", "A2",
                    50.0, "ml", "2025-12-31", 20.0, "Good"), // Above minimum
            Chemical(3, "PN003", "LOT003", "Chemical 3", "Manufacturer", "Adhesive", "A3",
                    15.0, "ml", "2025-12-31", 15.0, "Low Stock") // Equal to minimum
        )

        // When
        chemicalDao.insertChemicals(chemicals)
        val lowStockChemicals = chemicalDao.getLowStockChemicals().first()

        // Then
        assertEquals(2, lowStockChemicals.size)
        assertTrue(lowStockChemicals.all { it.quantity <= it.minimumStockLevel })
    }

    @Test
    fun issueChemical_success() = runTest {
        // Given
        val chemical = Chemical(
            id = 1,
            partNumber = "PN001",
            lotNumber = "LOT001",
            description = "Test Chemical",
            manufacturer = "Test Manufacturer",
            category = "Sealant",
            location = "A1",
            quantity = 100.0,
            unit = "ml",
            expirationDate = "2025-12-31",
            minimumStockLevel = 10.0,
            status = "Good"
        )
        chemicalDao.insertChemical(chemical)

        // When
        val success = chemicalDao.issueChemical(1, 30.0, 1) // userId = 1
        val updatedChemical = chemicalDao.getChemicalById(1)

        // Then
        assertTrue(success)
        assertNotNull(updatedChemical)
        assertEquals(70.0, updatedChemical?.quantity ?: 0.0, 0.01)
    }

    @Test
    fun issueChemical_insufficientQuantity() = runTest {
        // Given
        val chemical = Chemical(
            id = 1,
            partNumber = "PN001",
            lotNumber = "LOT001",
            description = "Test Chemical",
            manufacturer = "Test Manufacturer",
            category = "Sealant",
            location = "A1",
            quantity = 20.0,
            unit = "ml",
            expirationDate = "2025-12-31",
            minimumStockLevel = 10.0,
            status = "Good"
        )
        chemicalDao.insertChemical(chemical)

        // When
        val success = chemicalDao.issueChemical(1, 30.0, 1) // userId = 1
        val unchangedChemical = chemicalDao.getChemicalById(1)

        // Then
        assertFalse(success)
        assertNotNull(unchangedChemical)
        assertEquals(20.0, unchangedChemical?.quantity ?: 0.0, 0.01) // Quantity unchanged
    }
}
