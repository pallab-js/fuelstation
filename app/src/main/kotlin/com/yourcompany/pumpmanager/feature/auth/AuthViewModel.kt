package com.yourcompany.pumpmanager.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pumpmanager.core.security.PinHasher
import com.yourcompany.pumpmanager.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PinDigitEntered -> {
                if (_state.value.pinInput.length < 4) {
                    _state.update { it.copy(pinInput = it.pinInput + event.digit) }
                    if (_state.value.pinInput.length == 4) validatePin(_state.value.pinInput)
                }
            }
            AuthEvent.PinDeleted -> {
                if (_state.value.pinInput.isNotEmpty())
                    _state.update { it.copy(pinInput = it.pinInput.dropLast(1)) }
            }
            AuthEvent.BiometricTriggered -> authenticateWithBiometric()
            AuthEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun validatePin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = userDao.getCurrentUser()
            if (user != null && PinHasher.verify(pin, user.pinHash)) {
                sessionManager.setUser(user.id)
                _state.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Invalid PIN", pinInput = "") }
            }
        }
    }

    private fun authenticateWithBiometric() {
        viewModelScope.launch {
            val user = userDao.getCurrentUser() ?: return@launch
            sessionManager.setUser(user.id)
            _state.update { it.copy(isAuthenticated = true) }
        }
    }
}
