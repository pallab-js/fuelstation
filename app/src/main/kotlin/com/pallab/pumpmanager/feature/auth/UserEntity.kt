package com.pallab.pumpmanager.feature.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,           // "manager" | "attendant"
    val pinHash: String
)
