package com.pallab.pumpmanager.core.util

import javax.inject.Inject
import javax.inject.Singleton

interface Clock {
    fun now(): Long
}

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun now() = System.currentTimeMillis()
}
