package com.yourcompany.pumpmanager.feature.reports

import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.sales.SaleEntity
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val saleDao: SaleDao = mockk()
    private lateinit var viewModel: ReportsViewModel

    private fun sale(daysAgo: Int, amount: Double) = SaleEntity(
        id = "s-$daysAgo-$amount",
        shiftId = "shift-1",
        fuelType = "Petrol",
        volumeLiters = 10.0,
        pricePerLiter = amount / 10,
        totalAmount = amount,
        paymentMode = "CASH",
        timestamp = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }.timeInMillis
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `TODAY period only includes today sales`() = runTest {
        val sales = listOf(sale(0, 500.0), sale(1, 300.0), sale(2, 200.0))
        every { saleDao.getAllSales() } returns flowOf(sales)
        viewModel = ReportsViewModel(saleDao)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(500.0, viewModel.state.value.totalRevenueToday, 0.01)
        assertEquals(1, viewModel.state.value.totalSalesCountToday)
    }

    @Test
    fun `WEEK period includes last 7 days`() = runTest {
        val sales = listOf(sale(0, 100.0), sale(3, 200.0), sale(6, 300.0), sale(8, 999.0))
        every { saleDao.getAllSales() } returns flowOf(sales)
        viewModel = ReportsViewModel(saleDao)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.WEEK))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(600.0, viewModel.state.value.totalRevenueToday, 0.01)
        assertEquals(3, viewModel.state.value.totalSalesCountToday)
    }

    @Test
    fun `MONTH period includes last 30 days`() = runTest {
        val sales = listOf(sale(0, 100.0), sale(15, 200.0), sale(29, 300.0), sale(31, 999.0))
        every { saleDao.getAllSales() } returns flowOf(sales)
        viewModel = ReportsViewModel(saleDao)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.MONTH))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(600.0, viewModel.state.value.totalRevenueToday, 0.01)
        assertEquals(3, viewModel.state.value.totalSalesCountToday)
    }

    @Test
    fun `fuel type breakdown groups correctly`() = runTest {
        val sales = listOf(
            sale(0, 100.0).copy(fuelType = "Petrol"),
            sale(0, 200.0).copy(id = "s2", fuelType = "Diesel")
        )
        every { saleDao.getAllSales() } returns flowOf(sales)
        viewModel = ReportsViewModel(saleDao)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(100.0, viewModel.state.value.fuelTypeBreakdown["Petrol"], 0.01)
        assertEquals(200.0, viewModel.state.value.fuelTypeBreakdown["Diesel"], 0.01)
    }
}
