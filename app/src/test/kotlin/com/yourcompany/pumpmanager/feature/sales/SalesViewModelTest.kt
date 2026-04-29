package com.yourcompany.pumpmanager.feature.sales

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val saleDao: SaleDao = mockk(relaxed = true)
    private lateinit var viewModel: SalesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SalesViewModel(saleDao)
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
        viewModel.onEvent(SalesEvent.FuelSelected(FuelType.PETROL)) // Price 102.5
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))

        assertEquals(1025.0, viewModel.state.value.calculatedTotal, 0.001)
    }

    @Test
    fun `deleting volume digit recalculates total`() = runTest {
        viewModel.onEvent(SalesEvent.FuelSelected(FuelType.PETROL))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("1"))
        viewModel.onEvent(SalesEvent.VolumeDigitEntered("0"))
        viewModel.onEvent(SalesEvent.VolumeDeleted)

        assertEquals(102.5, viewModel.state.value.calculatedTotal, 0.001)
    }
}
