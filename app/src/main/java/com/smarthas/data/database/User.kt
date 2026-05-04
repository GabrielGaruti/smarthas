package com.smarthas.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val fullName: String,
    val createdAt: Long = System.currentTimeMillis()
)
