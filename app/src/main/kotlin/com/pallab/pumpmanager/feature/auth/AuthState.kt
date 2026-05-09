package com.pallab.pumpmanager.feature.auth

data class AuthUiState(
    val pinInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0L,
    val users: List<UserEntity> = emptyList(),
    val selectedUserId: String? = null
)
