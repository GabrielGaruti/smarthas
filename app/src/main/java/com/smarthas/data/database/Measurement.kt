package com.smarthas.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val systolic: Int,
    val diastolic: Int,
    val date: String, // Format: yyyy-MM-dd
    val time: String, // Format: HH:mm
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
