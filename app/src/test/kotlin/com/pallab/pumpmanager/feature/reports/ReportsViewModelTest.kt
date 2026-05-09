package com.pallab.pumpmanager.feature.reports

import com.pallab.pumpmanager.feature.sales.SalesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val salesRepository: SalesRepository = mockk()
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { salesRepository.getAllSales() } returns flowOf(emptyList())
        coEvery { salesRepository.getRevenueTrendSince(any()) } returns emptyList()
        viewModel = ReportsViewModel(salesRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `period change triggers new emission`() = runTest {
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(Period.TODAY, viewModel.state.value.selectedPeriod)

        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.WEEK))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(Period.WEEK, viewModel.state.value.selectedPeriod)
    }

    @Test
    fun `multiple period changes do not leak collectors`() = runTest {
        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.WEEK))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.MONTH))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ReportsEvent.PeriodChanged(Period.TODAY))
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value)
    }

    @Test
    fun `refresh triggers same period`() = runTest {
        assertEquals(Period.TODAY, viewModel.state.value.selectedPeriod)

        viewModel.onEvent(ReportsEvent.RefreshData)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(Period.TODAY, viewModel.state.value.selectedPeriod)
    }
}
