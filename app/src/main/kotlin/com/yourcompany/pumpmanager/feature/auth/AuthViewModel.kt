package com.yourcompany.pumpmanager.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PinDigitEntered -> {
                if (_state.value.pinInput.length < 4) {
                    _state.update { it.copy(pinInput = it.pinInput + event.digit) }
                    if (_state.value.pinInput.length == 4) {
                        validatePin(_state.value.pinInput)
                    }
                }
            }
            AuthEvent.PinDeleted -> {
                if (_state.value.pinInput.isNotEmpty()) {
                    _state.update { it.copy(pinInput = it.pinInput.dropLast(1)) }
                }
            }
            AuthEvent.BiometricTriggered -> {
                // Simulated biometric success
                _state.update { it.copy(isAuthenticated = true) }
            }
            AuthEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun validatePin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000) // Simulate network/DB check
            val isValid = verifyPin(pin)
            if (isValid) {
                _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Invalid PIN", pinInput = "") }
            }
        }
    }

    private suspend fun verifyPin(pin: String): Boolean {
        // In a real application, this would check against a secure hash in the database
        // or call an authentication service. For this demo, we'll simulate a check.
        return pin == "1234" 
    }
}
