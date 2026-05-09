package com.pallab.pumpmanager.feature.auth

import com.pallab.pumpmanager.core.security.PinHasher
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.Clock
import io.mockk.coEvery
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private val clock = mockk<Clock>()
    private val sessionManager = SessionManager(clock)
    private lateinit var viewModel: AuthViewModel

    private val salt = "test-salt"
    private val validUser = UserEntity(
        id = "user-1",
        name = "Admin",
        role = "manager",
        pinHash = PinHasher.hash("1234", salt)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.getAllUsers() } returns flowOf(listOf(validUser))
        every { clock.now() } returns 1000L
        viewModel = AuthViewModel(authRepository, sessionManager)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not authenticated`() = runTest {
        assertFalse(viewModel.state.value.isAuthenticated)
        assertEquals("", viewModel.state.value.pinInput)
    }

    @Test
    fun `valid PIN authenticates and sets session`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns validUser

        "1234".forEach { viewModel.onEvent(AuthEvent.PinDigitEntered(it.toString())) }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertEquals("user-1", sessionManager.currentUserId.value)
    }

    @Test
    fun `invalid PIN shows error and clears input`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns validUser

        "9999".forEach { viewModel.onEvent(AuthEvent.PinDigitEntered(it.toString())) }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthenticated)
        assertEquals("Invalid PIN", viewModel.state.value.errorMessage)
        assertEquals("", viewModel.state.value.pinInput)
    }

    @Test
    fun `null user shows error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        "1234".forEach { viewModel.onEvent(AuthEvent.PinDigitEntered(it.toString())) }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isAuthenticated)
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `biometric trigger sets session and authenticates`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns validUser

        viewModel.onEvent(AuthEvent.BiometricTriggered)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertEquals("user-1", sessionManager.currentUserId.value)
    }

    @Test
    fun `pin delete removes last digit`() = runTest {
        viewModel.onEvent(AuthEvent.PinDigitEntered("1"))
        viewModel.onEvent(AuthEvent.PinDigitEntered("2"))
        viewModel.onEvent(AuthEvent.PinDeleted)

        assertEquals("1", viewModel.state.value.pinInput)
    }

    @Test
    fun `dismiss error clears error message`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        "1234".forEach { viewModel.onEvent(AuthEvent.PinDigitEntered(it.toString())) }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AuthEvent.DismissError)
        assertNull(viewModel.state.value.errorMessage)
    }
}
