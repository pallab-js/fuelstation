package com.yourcompany.pumpmanager.feature.inventory

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
    private val tankDao: TankDao = mockk(relaxed = true)
    private lateinit var viewModel: InventoryViewModel

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `seeds tanks when DB is empty`() = runTest {
        Dispatchers.setMain(testDispatcher)
        every { tankDao.getAllTanks() } returns flowOf(emptyList())
        viewModel = InventoryViewModel(tankDao)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 3) { tankDao.insertTank(any()) }
    }

    @Test
    fun `does not seed when tanks already exist`() = runTest {
        Dispatchers.setMain(testDispatcher)
        val existing = listOf(TankEntity("t1", "petrol", 10000.0, 8000.0))
        every { tankDao.getAllTanks() } returns flowOf(existing)
        viewModel = InventoryViewModel(tankDao)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { tankDao.insertTank(any()) }
    }

    @Test
    fun `isLowStock returns true when stock below 10 percent`() = runTest {
        Dispatchers.setMain(testDispatcher)
        every { tankDao.getAllTanks() } returns flowOf(emptyList())
        viewModel = InventoryViewModel(tankDao)

        val lowTank = TankEntity("t1", "cng", 5000.0, 499.0)  // 9.98% < 10%
        assertTrue(viewModel.isLowStock(lowTank))
    }

    @Test
    fun `isLowStock returns false when stock above 10 percent`() = runTest {
        Dispatchers.setMain(testDispatcher)
        every { tankDao.getAllTanks() } returns flowOf(emptyList())
        viewModel = InventoryViewModel(tankDao)

        val okTank = TankEntity("t1", "petrol", 10000.0, 8000.0)  // 80%
        assertFalse(viewModel.isLowStock(okTank))
    }
}
