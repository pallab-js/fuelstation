package com.pallab.pumpmanager.feature.inventory

import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val inventoryRepository: InventoryRepository = mockk(relaxed = true)
    private val clock = mockk<Clock>()
    private val idGenerator = mockk<IdGenerator>()
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { inventoryRepository.getAllTanks() } returns flowOf(emptyList())
        every { clock.now() } returns 1000L
        every { idGenerator.newId() } returns "id-1"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isLowStock returns true when stock below 10 percent`() = runTest {
        every { inventoryRepository.getAllTanks() } returns flowOf(emptyList())
        viewModel = InventoryViewModel(inventoryRepository, idGenerator, clock)

        val lowTank = TankEntity("t1", "cng", 5000.0, 499.0)
        assertTrue(viewModel.isLowStock(lowTank))
    }

    @Test
    fun `isLowStock returns false when stock above 10 percent`() = runTest {
        every { inventoryRepository.getAllTanks() } returns flowOf(emptyList())
        viewModel = InventoryViewModel(inventoryRepository, idGenerator, clock)

        val okTank = TankEntity("t1", "petrol", 10000.0, 8000.0)
        assertFalse(viewModel.isLowStock(okTank))
    }
}
