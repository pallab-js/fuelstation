package com.yourcompany.pumpmanager.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentShiftId = MutableStateFlow<String?>(null)
    val currentShiftId: StateFlow<String?> = _currentShiftId

    fun setUser(id: String) { _currentUserId.value = id }
    fun setShift(id: String) { _currentShiftId.value = id }
    fun clearShift() { _currentShiftId.value = null }
    fun clearAll() { _currentUserId.value = null; _currentShiftId.value = null }
}
