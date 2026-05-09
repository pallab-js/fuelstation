package com.pallab.pumpmanager.core.util

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface IdGenerator {
    fun newId(): String
}

@Singleton
class UuidGenerator @Inject constructor() : IdGenerator {
    override fun newId() = UUID.randomUUID().toString()
}
