package com.pallab.pumpmanager.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.security.PinHasher
import com.pallab.pumpmanager.feature.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SetPinUiState(
    val pinInput: String = "",
    val isConfirming: Boolean = false,
    val firstPin: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPinSet: Boolean = false
)

sealed interface SetPinEvent {
    data class DigitEntered(val digit: String) : SetPinEvent
    object Delete : SetPinEvent
}

@HiltViewModel
class SetPinViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SetPinUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: SetPinEvent) {
        when (event) {
            is SetPinEvent.DigitEntered -> handleDigit(event.digit)
            SetPinEvent.Delete -> {
                if (_state.value.pinInput.isNotEmpty()) {
                    _state.update { it.copy(pinInput = it.pinInput.dropLast(1), errorMessage = null) }
                }
            }
        }
    }

    private fun handleDigit(digit: String) {
        val s = _state.value
        if (s.pinInput.length >= 4) return

        val newInput = s.pinInput + digit
        _state.update { it.copy(pinInput = newInput, errorMessage = null) }

        if (newInput.length == 4) {
            if (!s.isConfirming) {
                _state.update { it.copy(pinInput = "", isConfirming = true, firstPin = newInput) }
            } else {
                if (newInput == s.firstPin) {
                    savePin(newInput)
                } else {
                    _state.update {
                        it.copy(pinInput = "", isConfirming = false, firstPin = "", errorMessage = "PINs do not match. Try again.")
                    }
                }
            }
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val salt = UUID.randomUUID().toString()
                    val hashed = PinHasher.hash(pin, salt)
                    authRepository.updatePinHash(user.id, hashed)
                }
                _state.update { it.copy(isLoading = false, isPinSet = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to save PIN: ${e.message}") }
            }
        }
    }
}
