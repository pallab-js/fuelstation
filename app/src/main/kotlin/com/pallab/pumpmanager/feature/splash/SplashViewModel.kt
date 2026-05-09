package com.pallab.pumpmanager.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.feature.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashState {
    object Loading : SplashState
    object HasPin : SplashState
    object NeedsPinSetup : SplashState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            val user = authRepository.getCurrentUser()
            _state.value = when {
                user == null || user.pinHash.isBlank() -> SplashState.NeedsPinSetup
                else -> SplashState.HasPin
            }
        }
    }
}
