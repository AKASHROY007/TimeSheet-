package com.example.data.model

data class AppSettings(
    val userName: String = "",
    val trade: String = "",
    val language: String = "en", // "en", "bn", "ar"
    val themeMode: String = "dark", // "dark", "light"
    val weeklyOffDays: List<Int> = listOf(5), // 5 = Friday. 0=Sun, 1=Mon, ..., 5=Fri, 6=Sat
    val showEarningsInReport: Boolean = false,
    val defaultOtMultiplier: Double = 1.0
)
