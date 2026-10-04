package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_sheets")
data class TimeSheetEntity(
    @PrimaryKey val id: String,
    val month: String, // format: "YYYY-MM"
    val company: String = "",
    val subContractor: String = "",
    val hourlyRate: Double = 0.0,
    val currency: String = "BHD",
    val otMultiplier: Double = 1.0,
    val createdAt: Long = System.currentTimeMillis()
)
