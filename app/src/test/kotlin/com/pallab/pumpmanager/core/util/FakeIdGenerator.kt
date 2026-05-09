package com.pallab.pumpmanager.core.util

class FakeIdGenerator : IdGenerator {
    private var counter = 0
    override fun newId(): String = "id-${++counter}"
}
