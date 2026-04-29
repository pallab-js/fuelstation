package com.yourcompany.pumpmanager.feature.auth

sealed interface AuthEvent {
    data class PinDigitEntered(val digit: String) : AuthEvent
    object PinDeleted : AuthEvent
    object BiometricTriggered : AuthEvent
    object DismissError : AuthEvent
}
