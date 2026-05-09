package com.pallab.pumpmanager.core.session

import com.pallab.pumpmanager.core.util.BusinessConstants
import com.pallab.pumpmanager.core.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val clock: Clock) {
    private var lastActiveMs = 0L

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole

    private val _currentShiftId = MutableStateFlow<String?>(null)
    val currentShiftId: StateFlow<String?> = _currentShiftId

    fun setUser(id: String, role: String = "") { _currentUserId.value = id; _currentUserRole.value = role; touch() }
    fun setShift(id: String) { _currentShiftId.value = id }
    fun clearShift() { _currentShiftId.value = null }
    fun clearAll() { _currentUserId.value = null; _currentUserRole.value = null; _currentShiftId.value = null; lastActiveMs = 0L }

    fun touch() { lastActiveMs = clock.now() }
    fun isExpired(): Boolean {
        if (_currentUserId.value == null) return false
        return lastActiveMs > 0 && (clock.now() - lastActiveMs) > BusinessConstants.SESSION_TIMEOUT_MS
    }
}
