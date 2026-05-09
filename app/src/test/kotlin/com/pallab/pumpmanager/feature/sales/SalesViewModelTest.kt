package com.pallab.pumpmanager.feature.sales

import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import io.mockk.coEvery
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
class SalesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val salesRepository: SalesRepository = mockk(relaxed = true)
    private val inventoryRepository: InventoryRepository = mockk(relaxed = true)
    private val clock = mockk<Clock>()
    private val sessionManager = SessionManager(clock)
    private val idGenerator = mockk<IdGenerator>()
    private lateinit var viewModel: SalesViewModel

    private val petrol = FuelTypeEntity("petrol", "Petrol", 102.50)
    private val diesel = FuelTypeEntity("diesel", "Diesel", 94.20)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { inventoryRepository.getActiveFuelTypes() } returns flowOf(listOf(petrol, diesel))
        every { clock.now() } returns 1000L
        every { idGenerator.newId() } returns "sale-1"
        viewModel = SalesViewModel(salesRepository, inventoryRepository, sessionManager, clock, idGenerator)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting fuel updates price`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(diesel))
        assertEquals(diesel.id, viewModel.state.value.selectedFuel?.id)
        assertEquals(94.20, viewModel.state.value.pricePerLiter, 0.001)
    }

    @Test
    fun `entering volume calculates total correctly`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(petrol))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))
        assertEquals(1025.0, viewModel.state.value.calculatedTotal, 0.001)
    }

    @Test
    fun `duplicate decimal point is blocked`() = runTest {
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("."))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("."))
        assertEquals("1.5", viewModel.state.value.volume)
    }

    @Test
    fun `max volume guard shows error`() = runTest {
        sessionManager.setShift("shift-1")
        "9999".forEach { viewModel.onEvent(SalesEvent.VolumeDigitEntered(it.toString())) }
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("9"))
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `no active shift shows error on save`() = runTest {
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("No active shift. Please start a shift first.", viewModel.state.value.errorMessage)
    }

    @Test
    fun `save sale succeeds with active shift`() = runTest {
        coEvery { inventoryRepository.decrementStock(any(), any()) } returns 1
        sessionManager.setShift("shift-1")
        viewModel.onEvent(SalesEvent.FuelSelected(petrol))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.isSuccess)
        coVerify { salesRepository.insertSale(any()) }
    }

    @Test
    fun `volume delete recalculates total`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(petrol))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))
        viewModel.onEvent(SalesEvent.VolumeDeleted)
        assertEquals(102.5, viewModel.state.value.calculatedTotal, 0.001)
    }
}
