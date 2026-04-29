package com.yourcompany.pumpmanager.feature.auth

data class AuthUiState(
    val pinInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)
