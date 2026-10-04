package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.TimeSheetDao
import com.example.data.model.AdvanceEntity
import com.example.data.model.AppSettings
import com.example.data.model.DayRecordEntity
import com.example.data.model.TimeSheetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class TimeSheetRepository(
    private val timeSheetDao: TimeSheetDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("timesheet_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val userName = prefs.getString("user_name", "") ?: ""
        val trade = prefs.getString("trade", "") ?: ""
        val language = prefs.getString("language", "en") ?: "en"
        val themeMode = prefs.getString("theme_mode", "dark") ?: "dark"
        val weeklyOffString = prefs.getString("weekly_off", "5") ?: "5" // Default Friday (5)
        val weeklyOffDays = if (weeklyOffString.isBlank()) {
            emptyList()
        } else {
            weeklyOffString.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
        val showEarningsInReport = prefs.getBoolean("show_earnings_in_report", false)
        val defaultOtMultiplier = prefs.getFloat("default_ot_multiplier", 1.0f).toDouble()

        return AppSettings(
            userName = userName,
            trade = trade,
            language = language,
            themeMode = themeMode,
            weeklyOffDays = weeklyOffDays,
            showEarningsInReport = showEarningsInReport,
            defaultOtMultiplier = defaultOtMultiplier
        )
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) = withContext(Dispatchers.IO) {
        val updated = transform(_settingsFlow.value)
        prefs.edit()
            .putString("user_name", updated.userName)
            .putString("trade", updated.trade)
            .putString("language", updated.language)
            .putString("themeMode", updated.themeMode)
            .putString("weekly_off", updated.weeklyOffDays.joinToString(","))
            .putBoolean("show_earnings_in_report", updated.showEarningsInReport)
            .putFloat("default_ot_multiplier", updated.defaultOtMultiplier.toFloat())
            .apply()
        _settingsFlow.value = updated
    }

    fun getSheetsForMonth(month: String): Flow<List<TimeSheetEntity>> =
        timeSheetDao.getSheetsForMonth(month)

    fun getAllSheets(): Flow<List<TimeSheetEntity>> =
        timeSheetDao.getAllSheets()

    suspend fun getSheetById(id: String): TimeSheetEntity? =
        timeSheetDao.getSheetById(id)

    suspend fun createSheet(month: String, company: String = "", subContractor: String = "", rate: Double = 0.0, currency: String = "BHD", otMultiplier: Double = 1.0): TimeSheetEntity {
        val newSheet = TimeSheetEntity(
            id = "s_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            month = month,
            company = company,
            subContractor = subContractor,
            hourlyRate = rate,
            currency = currency,
            otMultiplier = otMultiplier,
            createdAt = System.currentTimeMillis()
        )
        timeSheetDao.insertOrUpdateSheet(newSheet)
        return newSheet
    }

    suspend fun saveSheet(sheet: TimeSheetEntity) = withContext(Dispatchers.IO) {
        timeSheetDao.insertOrUpdateSheet(sheet)
    }

    suspend fun deleteSheet(sheetId: String) = withContext(Dispatchers.IO) {
        timeSheetDao.deleteRecordsForSheet(sheetId)
        timeSheetDao.deleteAdvancesForSheet(sheetId)
        timeSheetDao.deleteSheet(sheetId)
    }

    fun getRecordsForSheet(sheetId: String): Flow<List<DayRecordEntity>> =
        timeSheetDao.getRecordsForSheet(sheetId)

    suspend fun saveRecord(record: DayRecordEntity) = withContext(Dispatchers.IO) {
        timeSheetDao.insertOrUpdateRecord(record)
    }

    suspend fun saveRecords(records: List<DayRecordEntity>) = withContext(Dispatchers.IO) {
        timeSheetDao.insertRecords(records)
    }

    fun getAdvancesForSheet(sheetId: String): Flow<List<AdvanceEntity>> =
        timeSheetDao.getAdvancesForSheet(sheetId)

    suspend fun addAdvance(sheetId: String, date: String, amount: Double, note: String) = withContext(Dispatchers.IO) {
        timeSheetDao.insertAdvance(
            AdvanceEntity(
                sheetId = sheetId,
                date = date,
                amount = amount,
                note = note
            )
        )
    }

    suspend fun deleteAdvance(id: Long) = withContext(Dispatchers.IO) {
        timeSheetDao.deleteAdvance(id)
    }

    fun getAllPresentRecords(): Flow<List<DayRecordEntity>> =
        timeSheetDao.getAllPresentRecords()

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        timeSheetDao.clearAllRecords()
        timeSheetDao.clearAllAdvances()
        timeSheetDao.clearAllSheets()
        prefs.edit().clear().apply()
        _settingsFlow.value = AppSettings()
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "timesheet")
        root.put("version", 3)

        val userObj = JSONObject()
        userObj.put("name", _settingsFlow.value.userName)
        userObj.put("trade", _settingsFlow.value.trade)
        root.put("user", userObj)

        val offArr = JSONArray()
        _settingsFlow.value.weeklyOffDays.forEach { offArr.put(it) }
        root.put("off", offArr)

        val sheetsObj = JSONObject()
        val allSheets = withContext(Dispatchers.IO) {
            // Read all sheets directly from DB
            timeSheetDao.getSheetById("") // placeholder check
            val list = mutableListOf<TimeSheetEntity>()
            // Using a simple query if needed, or getAllSheets collect
            list
        }
        // Build JSON representation compatible with the web timesheet_v3 format
        root.toString(2)
    }

    suspend fun exportCompleteJson(allSheets: List<TimeSheetEntity>): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "timesheet")
        root.put("version", 3)

        val userObj = JSONObject()
        userObj.put("name", _settingsFlow.value.userName)
        userObj.put("trade", _settingsFlow.value.trade)
        root.put("user", userObj)

        val offArr = JSONArray()
        _settingsFlow.value.weeklyOffDays.forEach { offArr.put(it) }
        root.put("off", offArr)

        val sheetsObj = JSONObject()
        for (sheet in allSheets) {
            val sObj = JSONObject()
            sObj.put("m", sheet.month)
            sObj.put("co", sheet.company)
            sObj.put("sub", sheet.subContractor)
            sObj.put("rate", sheet.hourlyRate.toString())
            sObj.put("cur", sheet.currency)
            sObj.put("otx", sheet.otMultiplier)
            sObj.put("t", sheet.createdAt)
            sObj.put("keep", 1)

            val recsObj = JSONObject()
            val recList = timeSheetDao.getRecordsListForSheet(sheet.id)
            for (rec in recList) {
                if (rec.status.isNotBlank()) {
                    val rObj = JSONObject()
                    rObj.put("status", rec.status)
                    rObj.put("basic", rec.basicHours)
                    rObj.put("ot", rec.otHours)
                    rec.customOtMultiplier?.let { rObj.put("otx", it) }
                    recsObj.put(rec.date, rObj)
                }
            }
            sObj.put("recs", recsObj)

            val advArr = JSONArray()
            val advList = timeSheetDao.getAdvancesListForSheet(sheet.id)
            for (adv in advList) {
                val aObj = JSONObject()
                aObj.put("d", adv.date)
                aObj.put("a", adv.amount)
                aObj.put("n", adv.note)
                advArr.put(aObj)
            }
            sObj.put("adv", advArr)

            sheetsObj.put(sheet.id, sObj)
        }
        root.put("sheets", sheetsObj)

        root.toString(2)
    }

    suspend fun importJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("user")) {
                val u = root.getJSONObject("user")
                val name = u.optString("name", "")
                val tr = u.optString("trade", "")
                updateSettings { it.copy(userName = name, trade = tr) }
            }
            if (root.has("off")) {
                val offArr = root.getJSONArray("off")
                val offs = mutableListOf<Int>()
                for (i in 0 until offArr.length()) {
                    offs.add(offArr.getInt(i))
                }
                updateSettings { it.copy(weeklyOffDays = offs) }
            }
            if (root.has("sheets")) {
                val sheetsObj = root.getJSONObject("sheets")
                val keys = sheetsObj.keys()
                while (keys.hasNext()) {
                    val sheetId = keys.next()
                    val sObj = sheetsObj.getJSONObject(sheetId)
                    val month = sObj.optString("m", "")
                    if (month.isNotBlank()) {
                        val sheet = TimeSheetEntity(
                            id = sheetId,
                            month = month,
                            company = sObj.optString("co", ""),
                            subContractor = sObj.optString("sub", ""),
                            hourlyRate = sObj.optString("rate", "0").toDoubleOrNull() ?: 0.0,
                            currency = sObj.optString("cur", "BHD"),
                            otMultiplier = sObj.optDouble("otx", 1.0),
                            createdAt = sObj.optLong("t", System.currentTimeMillis())
                        )
                        timeSheetDao.insertOrUpdateSheet(sheet)

                        if (sObj.has("recs")) {
                            val recsObj = sObj.getJSONObject("recs")
                            val rKeys = recsObj.keys()
                            val recs = mutableListOf<DayRecordEntity>()
                            while (rKeys.hasNext()) {
                                val date = rKeys.next()
                                val rObj = recsObj.getJSONObject(date)
                                recs.add(
                                    DayRecordEntity(
                                        id = "${sheetId}_$date",
                                        sheetId = sheetId,
                                        date = date,
                                        status = rObj.optString("status", ""),
                                        basicHours = rObj.optDouble("basic", 0.0),
                                        otHours = rObj.optDouble("ot", 0.0),
                                        customOtMultiplier = if (rObj.has("otx")) rObj.optDouble("otx") else null
                                    )
                                )
                            }
                            timeSheetDao.insertRecords(recs)
                        }

                        if (sObj.has("adv")) {
                            val advArr = sObj.getJSONArray("adv")
                            for (i in 0 until advArr.length()) {
                                val aObj = advArr.getJSONObject(i)
                                timeSheetDao.insertAdvance(
                                    AdvanceEntity(
                                        sheetId = sheetId,
                                        date = aObj.optString("d", "$month-01"),
                                        amount = aObj.optDouble("a", 0.0),
                                        note = aObj.optString("n", "")
                                    )
                                )
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
