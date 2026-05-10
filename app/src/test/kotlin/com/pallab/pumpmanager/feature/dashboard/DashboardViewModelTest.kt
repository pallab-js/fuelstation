package com.pallab.pumpmanager.feature.dashboard

import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SalesRepository
import com.pallab.pumpmanager.feature.shift.ShiftEntity
import com.pallab.pumpmanager.feature.shift.ShiftRepository
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
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val salesRepository: SalesRepository = mockk()
    private val shiftRepository: ShiftRepository = mockk()
    private val sessionManager: SessionManager = mockk()
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { sessionManager.currentUserId } returns MutableStateFlow(null)
        every { sessionManager.currentUserRole } returns MutableStateFlow(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows revenue and sales count from today sales`() = runTest {
        val sales = listOf(
            SaleEntity("s1", "shift-1", "Petrol", 10.0, 100.0, 1000.0, "Cash", 1000L),
            SaleEntity("s2", "shift-1", "Diesel", 5.0, 90.0, 450.0, "Card", 1001L)
        )
        every { salesRepository.getTodaySales(any()) } returns flowOf(sales)
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1450.0, viewModel.state.value.todayRevenue, 0.01)
        assertEquals(2, viewModel.state.value.todaySalesCount)
        assertNull(viewModel.state.value.activeShift)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `shows no data when there are no sales`() = runTest {
        every { salesRepository.getTodaySales(any()) } returns flowOf(emptyList())
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0.0, viewModel.state.value.todayRevenue, 0.01)
        assertEquals(0, viewModel.state.value.todaySalesCount)
        assertNull(viewModel.state.value.activeShift)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `shows active shift when one exists`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { salesRepository.getTodaySales(any()) } returns flowOf(emptyList())
        every { shiftRepository.getActiveShift() } returns flowOf(activeShift)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.activeShift)
        assertEquals("s1", viewModel.state.value.activeShift?.id)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `shows error when repository throws`() = runTest {
        every { salesRepository.getTodaySales(any()) } throws RuntimeException("DB error")
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.errorMessage!!.contains("DB error"))
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `refresh clears error and reloads`() = runTest {
        every { salesRepository.getTodaySales(any()) } throws RuntimeException("DB error")
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)

        every { salesRepository.getTodaySales(any()) } returns flowOf(emptyList())

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.errorMessage)
        assertEquals(0.0, viewModel.state.value.todayRevenue, 0.01)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        every { salesRepository.getTodaySales(any()) } throws RuntimeException("DB error")
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)

        viewModel.onErrorDismissed()

        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `starts in loading state`() = runTest {
        val salesFlow = kotlinx.coroutines.flow.MutableStateFlow<List<SaleEntity>>(emptyList())
        every { salesRepository.getTodaySales(any()) } returns salesFlow
        every { shiftRepository.getActiveShift() } returns flowOf(null)

        viewModel = DashboardViewModel(salesRepository, shiftRepository, sessionManager)

        assertTrue(viewModel.state.value.isLoading)
    }
}
