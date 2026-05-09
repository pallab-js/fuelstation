package com.pallab.pumpmanager.core.util

class FakeClock(private var currentTime: Long = 0L) : Clock {
    override fun now(): Long = currentTime
    fun advance(ms: Long) { currentTime += ms }
}
