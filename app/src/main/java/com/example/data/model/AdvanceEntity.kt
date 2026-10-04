package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advances")
data class AdvanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sheetId: String,
    val date: String, // format: "YYYY-MM-DD"
    val amount: Double,
    val note: String = ""
)
