package com.pallab.pumpmanager.feature.shift

import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.feature.sales.SalesRepository
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
    private val shiftRepository: ShiftRepository = mockk(relaxed = true)
    private val salesRepository: SalesRepository = mockk(relaxed = true)
    private val clock = mockk<Clock>()
    private val sessionManager = SessionManager(clock)
    private val idGenerator = mockk<IdGenerator>()
    private lateinit var viewModel: ShiftViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { shiftRepository.getActiveShift() } returns flowOf(null)
        every { clock.now() } returns 1000L
        every { idGenerator.newId() } returns "shift-1"
        sessionManager.setUser("user-1")
        viewModel = ShiftViewModel(shiftRepository, salesRepository, sessionManager, clock, idGenerator)
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

        coVerify { shiftRepository.insertShift(any()) }
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
        every { shiftRepository.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftRepository, salesRepository, sessionManager, clock, idGenerator)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.OpeningMeterChanged("2000"))
        viewModel.onEvent(ShiftEvent.StartShift)

        assertEquals("A shift is already active", viewModel.state.value.errorMessage)
    }

    @Test
    fun `closing meter below opening shows error`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftRepository.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftRepository, salesRepository, sessionManager, clock, idGenerator)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("500"))
        viewModel.onEvent(ShiftEvent.EndShift)

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `closing meter exceeding threshold shows error`() = runTest {
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftRepository.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftRepository, salesRepository, sessionManager, clock, idGenerator)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("7000"))
        viewModel.onEvent(ShiftEvent.EndShift)

        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `end shift clears session shift`() = runTest {
        sessionManager.setShift("s1")
        val activeShift = ShiftEntity("s1", "user-1", 0L, null, 1000.0, null, "active")
        every { shiftRepository.getActiveShift() } returns flowOf(activeShift)
        viewModel = ShiftViewModel(shiftRepository, salesRepository, sessionManager, clock, idGenerator)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ShiftEvent.ClosingMeterChanged("1500"))
        viewModel.onEvent(ShiftEvent.EndShift)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(sessionManager.currentShiftId.value)
    }
}
