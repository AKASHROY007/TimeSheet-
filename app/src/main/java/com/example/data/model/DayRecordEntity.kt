package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_records",
    indices = [Index(value = ["sheetId", "date"], unique = true)]
)
data class DayRecordEntity(
    @PrimaryKey val id: String, // "${sheetId}_${date}"
    val sheetId: String,
    val date: String, // format: "YYYY-MM-DD"
    val status: String = "", // "" (not entered), "present", "absent", "holiday"
    val basicHours: Double = 0.0,
    val otHours: Double = 0.0,
    val customOtMultiplier: Double? = null // if null, defaults to sheet otMultiplier or 2.0 on weekly off
)
