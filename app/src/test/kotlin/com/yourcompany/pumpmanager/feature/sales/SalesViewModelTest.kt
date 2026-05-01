package com.yourcompany.pumpmanager.feature.sales

import com.yourcompany.pumpmanager.core.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val saleDao: SaleDao = mockk(relaxed = true)
    private val sessionManager = SessionManager()
    private lateinit var viewModel: SalesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SalesViewModel(saleDao, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting fuel updates price`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(FuelType.DIESEL))
        assertEquals(FuelType.DIESEL, viewModel.state.value.selectedFuel)
        assertEquals(FuelType.DIESEL.price, viewModel.state.value.pricePerLiter, 0.001)
    }

    @Test
    fun `entering volume calculates total correctly`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(FuelType.PETROL))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))
        assertEquals(1025.0, viewModel.state.value.calculatedTotal, 0.001)
    }

    @Test
    fun `duplicate decimal point is blocked`() = runTest {
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("."))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("."))  // should be ignored
        assertEquals("1.5", viewModel.state.value.volume)
    }

    @Test
    fun `max volume guard shows error`() = runTest {
        sessionManager.setShift("shift-1")
        "9999".forEach { viewModel.onEvent(SalesEvent.VolumeDigitEntered(it.toString())) }
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("9"))  // 99999 > 9999
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `no active shift shows error on save`() = runTest {
        // sessionManager has no shift set
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("No active shift. Please start a shift first.", viewModel.state.value.errorMessage)
    }

    @Test
    fun `save sale succeeds with active shift`() = runTest {
        sessionManager.setShift("shift-1")
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("5"))
        viewModel.onEvent(SalesEvent.SaveSale)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.isSuccess)
        coVerify { saleDao.insertSale(any()) }
    }

    @Test
    fun `volume delete recalculates total`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(FuelType.PETROL))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))
        viewModel.onEvent(SalesEvent.VolumeDeleted)
        assertEquals(102.5, viewModel.state.value.calculatedTotal, 0.001)
    }
}
