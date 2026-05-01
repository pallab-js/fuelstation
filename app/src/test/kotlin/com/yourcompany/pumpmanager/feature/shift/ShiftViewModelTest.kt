package com.yourcompany.pumpmanager.feature.shift

import com.yourcompany.pumpmanager.core.session.SessionManager
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
class ShiftViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val shiftDao: ShiftDao = mockk(relaxed = true)
    private val sessionManager = SessionManager()
    private lateinit var viewModel: ShiftViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { shiftDao.getActiveShift() } returns flowOf(null)
        sessionManager.setUser("user-1")
        viewModel = ShiftViewModel(shiftDao, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start shift inserts shift and sets session`() = runTest {
        viewModel.onEvent(ShiftEvent.OpeningMeterChanged("1000"))
        viewModel.onEvent(ShiftEvent.StartShift)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { shiftDao.insertShift(any()) }
        assertNotNull(sessionManager.currentShiftId.value)
    }

    @Test
    fun `start shift with no session shows error`() = runTest {
        sessionManager.clearAll()
        viewModel.onEvent(ShiftEvent.OpeningMeterChanged("1000"))
        viewModel.onEvent(ShiftEvent.StartShift)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("No active session", viewModel.state.value.errorMessage)
    }

    @Test
    fun `concurrent shift guard prevents second start`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftDao.getActiveShift() } returns flowOf(activeShift)
        // Re-create VM so it picks up the active shift
        viewModel = ShiftViewModel(shiftDao, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.OpeningMeterChanged("2000"))
        viewModel.onEvent(ShiftEvent.StartShift)

        assertEquals("A shift is already active", viewModel.state.value.errorMessage)
    }

    @Test
    fun `closing meter below opening shows error`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftDao.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftDao, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("500"))
        viewModel.onEvent(ShiftEvent.EndShift)

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `closing meter exceeding threshold shows error`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftDao.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftDao, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("7000")) // diff = 6000 > 5000
        viewModel.onEvent(ShiftEvent.EndShift)

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `end shift clears session shift`() = runTest {
        sessionManager.setShift("s1")
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftDao.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftDao, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("1500"))
        viewModel.onEvent(ShiftEvent.EndShift)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(sessionManager.currentShiftId.value)
    }
}
