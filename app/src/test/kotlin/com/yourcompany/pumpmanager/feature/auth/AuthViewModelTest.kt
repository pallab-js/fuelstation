package com.yourcompany.pumpmanager.feature.auth

import app.cash.turbine.test
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not authenticated`() = runTest {
        assertEquals("", viewModel.state.value.pin)
        assertEquals(false, viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `entering correct PIN authenticates user`() = runTest {
        viewModel.onEvent(AuthEvent.DigitEntered("1"))
        viewModel.onEvent(AuthEvent.DigitEntered("2"))
        viewModel.onEvent(AuthEvent.DigitEntered("3"))
        viewModel.onEvent(AuthEvent.DigitEntered("4"))

        assertEquals(true, viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `entering incorrect PIN shows error`() = runTest {
        viewModel.onEvent(AuthEvent.DigitEntered("1"))
        viewModel.onEvent(AuthEvent.DigitEntered("1"))
        viewModel.onEvent(AuthEvent.DigitEntered("1"))
        viewModel.onEvent(AuthEvent.DigitEntered("1"))

        assertEquals(false, viewModel.state.value.isAuthenticated)
        assertEquals("Invalid PIN", viewModel.state.value.errorMessage)
    }
}
