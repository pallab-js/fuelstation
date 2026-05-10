package com.pallab.pumpmanager.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.security.PinHasher
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.BusinessConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAllUsers().collect { users ->
                _state.update { it.copy(users = users) }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.PinDigitEntered -> {
                val s = _state.value
                if (s.lockedUntil > System.currentTimeMillis()) return
                if (s.pinInput.length < 4) {
                    _state.update { it.copy(pinInput = it.pinInput + event.digit, errorMessage = null) }
                    if (_state.value.pinInput.length == 4) validatePin(_state.value.pinInput)
                }
            }
            AuthEvent.PinDeleted -> {
                if (_state.value.pinInput.isNotEmpty())
                    _state.update { it.copy(pinInput = it.pinInput.dropLast(1)) }
            }
            AuthEvent.BiometricTriggered -> authenticateWithBiometric()
            AuthEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
            is AuthEvent.UserSelected -> {
                _state.update { it.copy(selectedUserId = event.userId, pinInput = "", errorMessage = null, failedAttempts = 0, lockedUntil = 0L) }
            }
        }
    }

    private fun validatePin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = _state.value.selectedUserId
            val user = if (userId != null) authRepository.getUserById(userId) else authRepository.getCurrentUser()
            if (user != null && PinHasher.verify(pin, user.pinHash)) {
                sessionManager.setUser(user.id, user.role)
                _state.update { it.copy(isLoading = false, isAuthenticated = true, failedAttempts = 0, lockedUntil = 0L) }
            } else {
                val attempts = _state.value.failedAttempts + 1
                val lockDuration = when {
                    attempts >= BusinessConstants.MAX_PIN_ATTEMPTS * 2 -> 10 * 60 * 1_000L
                    attempts >= BusinessConstants.MAX_PIN_ATTEMPTS + 2 -> 2 * 60 * 1_000L
                    attempts >= BusinessConstants.MAX_PIN_ATTEMPTS -> 30 * 1_000L
                    else -> 0L
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        failedAttempts = attempts,
                        lockedUntil = if (lockDuration > 0) System.currentTimeMillis() + lockDuration else it.lockedUntil,
                        errorMessage = if (lockDuration > 0) "Too many attempts. Try again in ${lockDuration / 1000}s" else "Invalid PIN",
                        pinInput = ""
                    )
                }
            }
        }
    }

    private fun authenticateWithBiometric() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            sessionManager.setUser(user.id, user.role)
            _state.update { it.copy(isAuthenticated = true) }
        }
    }
}
