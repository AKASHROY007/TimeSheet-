package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AdvanceEntity
import com.example.data.model.AppSettings
import com.example.data.model.DayRecordEntity
import com.example.data.model.TimeSheetEntity
import com.example.data.repository.TimeSheetRepository
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TimeSheetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeSheetRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimeSheetRepository(db.timeSheetDao(), application)
    }

    val settings: StateFlow<AppSettings> = repository.settingsFlow

    private val _currentMonth = MutableStateFlow(Localization.getCurrentMonthString())
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    val sheetsForMonth: StateFlow<List<TimeSheetEntity>> = _currentMonth
        .flatMapLatest { m -> repository.getSheetsForMonth(m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSheetId = MutableStateFlow<String?>(null)
    val selectedSheetId: StateFlow<String?> = _selectedSheetId.asStateFlow()

    val currentSheet: StateFlow<TimeSheetEntity?> = combine(sheetsForMonth, _selectedSheetId) { list, id ->
        when {
            list.isEmpty() -> null
            id != null -> list.find { it.id == id } ?: list.first()
            else -> list.first()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dayRecords: StateFlow<List<DayRecordEntity>> = currentSheet
        .flatMapLatest { sheet ->
            if (sheet != null) repository.getRecordsForSheet(sheet.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val advances: StateFlow<List<AdvanceEntity>> = currentSheet
        .flatMapLatest { sheet ->
            if (sheet != null) repository.getAdvancesForSheet(sheet.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All present records for lifetime hours and earnings
    val allSheets: StateFlow<List<TimeSheetEntity>> = repository.getAllSheets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresentRecords: StateFlow<List<DayRecordEntity>> = repository.getAllPresentRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog state
    var isSettingsOpen = MutableStateFlow(false)
    var isQuickFillOpen = MutableStateFlow(false)
    var isAddSheetOpen = MutableStateFlow(false)
    var sheetToDelete = MutableStateFlow<TimeSheetEntity?>(null)
    var isResetConfirmOpen = MutableStateFlow(false)
    var statusMessage = MutableStateFlow<String?>(null)

    init {
        // Ensure default sheet exists for current month if none
        viewModelScope.launch {
            sheetsForMonth.collect { list ->
                if (list.isEmpty()) {
                    val m = _currentMonth.value
                    val newSheet = repository.createSheet(
                        month = m,
                        rate = 0.0,
                        currency = "BHD",
                        otMultiplier = settings.value.defaultOtMultiplier
                    )
                    _selectedSheetId.value = newSheet.id
                } else if (_selectedSheetId.value == null || list.none { it.id == _selectedSheetId.value }) {
                    _selectedSheetId.value = list.first().id
                }
            }
        }
    }

    fun changeMonth(delta: Int) {
        val parts = _currentMonth.value.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 10

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m - 1 + delta)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val newY = cal.get(Calendar.YEAR)
        val newM = cal.get(Calendar.MONTH) + 1
        _currentMonth.value = String.format(Locale.US, "%04d-%02d", newY, newM)
        _selectedSheetId.value = null
    }

    fun setMonth(year: Int, month: Int) {
        _currentMonth.value = String.format(Locale.US, "%04d-%02d", year, month)
        _selectedSheetId.value = null
    }

    fun selectSheet(id: String) {
        _selectedSheetId.value = id
    }

    fun addNewSheet(company: String = "", subContractor: String = "") {
        viewModelScope.launch {
            val curr = currentSheet.value
            val m = _currentMonth.value
            val newSheet = repository.createSheet(
                month = m,
                company = company.ifBlank { curr?.company ?: "" },
                subContractor = subContractor.ifBlank { curr?.subContractor ?: "" },
                rate = curr?.hourlyRate ?: 0.0,
                currency = curr?.currency ?: "BHD",
                otMultiplier = curr?.otMultiplier ?: settings.value.defaultOtMultiplier
            )
            _selectedSheetId.value = newSheet.id
        }
    }

    fun deleteCurrentSheet() {
        val s = currentSheet.value ?: return
        viewModelScope.launch {
            repository.deleteSheet(s.id)
            val remaining = sheetsForMonth.value.filter { it.id != s.id }
            if (remaining.isNotEmpty()) {
                _selectedSheetId.value = remaining.first().id
            } else {
                val newSheet = repository.createSheet(
                    month = _currentMonth.value,
                    rate = s.hourlyRate,
                    currency = s.currency,
                    otMultiplier = s.otMultiplier
                )
                _selectedSheetId.value = newSheet.id
            }
        }
    }

    fun updateSheetMeta(company: String, subContractor: String) {
        val s = currentSheet.value ?: return
        viewModelScope.launch {
            repository.saveSheet(s.copy(company = company, subContractor = subContractor))
        }
    }

    fun updateSheetRate(rate: Double) {
        val s = currentSheet.value ?: return
        viewModelScope.launch {
            repository.saveSheet(s.copy(hourlyRate = rate.coerceIn(0.0, 1000000.0)))
        }
    }

    fun updateSheetCurrency(currency: String) {
        val s = currentSheet.value ?: return
        viewModelScope.launch {
            repository.saveSheet(s.copy(currency = currency))
        }
    }

    fun updateSheetOtMultiplier(otx: Double) {
        val s = currentSheet.value ?: return
        viewModelScope.launch {
            repository.saveSheet(s.copy(otMultiplier = otx))
        }
    }

    fun updateUserProfile(name: String, trade: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(userName = name, trade = trade) }
        }
    }

    fun toggleDayStatus(date: String) {
        val sheet = currentSheet.value ?: return
        viewModelScope.launch {
            val existing = dayRecords.value.find { it.date == date }
            val currentStatus = existing?.status ?: ""
            val nextStatus = when (currentStatus) {
                "" -> "present"
                "present" -> "absent"
                "absent" -> "holiday"
                "holiday" -> ""
                else -> ""
            }

            val basicHours = if (nextStatus == "present") 8.0 else 0.0
            val otHours = 0.0

            val updated = DayRecordEntity(
                id = "${sheet.id}_$date",
                sheetId = sheet.id,
                date = date,
                status = nextStatus,
                basicHours = basicHours,
                otHours = otHours,
                customOtMultiplier = existing?.customOtMultiplier
            )
            repository.saveRecord(updated)
        }
    }

    fun updateDayHours(date: String, basic: Double, ot: Double) {
        val sheet = currentSheet.value ?: return
        viewModelScope.launch {
            val existing = dayRecords.value.find { it.date == date }
            val clampedBasic = basic.coerceIn(0.0, 24.0)
            val clampedOt = ot.coerceIn(0.0, 24.0 - clampedBasic)

            val updated = DayRecordEntity(
                id = "${sheet.id}_$date",
                sheetId = sheet.id,
                date = date,
                status = existing?.status ?: "present",
                basicHours = clampedBasic,
                otHours = clampedOt,
                customOtMultiplier = existing?.customOtMultiplier
            )
            repository.saveRecord(updated)
        }
    }

    fun cycleDayOtMultiplier(date: String) {
        val sheet = currentSheet.value ?: return
        viewModelScope.launch {
            val existing = dayRecords.value.find { it.date == date } ?: return@launch
            if (existing.status != "present") return@launch

            val parts = date.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val isOff = settings.value.weeklyOffDays.contains(Localization.getDayOfWeek(y, m, d))
            val defaultOtx = if (isOff) 2.0 else sheet.otMultiplier
            val currentOtx = existing.customOtMultiplier ?: defaultOtx

            val rates = Localization.OT_RATES
            val nextRate = rates.find { it > currentOtx } ?: rates.first()
            val finalOtx = if (nextRate == defaultOtx) null else nextRate

            val updated = existing.copy(customOtMultiplier = finalOtx)
            repository.saveRecord(updated)
        }
    }

    fun quickFillMonth(
        basic: Double,
        ot: Double,
        skipWeeklyOff: Boolean,
        onlyEmpty: Boolean,
        upToToday: Boolean
    ) {
        val sheet = currentSheet.value ?: return
        viewModelScope.launch {
            val parts = _currentMonth.value.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val totalDays = Localization.getDaysInMonth(year, month)
            val todayStr = Localization.getCurrentDateString()
            val existingMap = dayRecords.value.associateBy { it.date }

            val list = mutableListOf<DayRecordEntity>()
            for (d in 1..totalDays) {
                val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
                if (upToToday && dateStr > todayStr) continue

                val dow = Localization.getDayOfWeek(year, month, d)
                if (skipWeeklyOff && settings.value.weeklyOffDays.contains(dow)) continue

                val existing = existingMap[dateStr]
                if (onlyEmpty && existing != null && existing.status.isNotBlank()) continue

                val b = basic.coerceIn(0.0, 24.0)
                val o = ot.coerceIn(0.0, 24.0 - b)
                list.add(
                    DayRecordEntity(
                        id = "${sheet.id}_$dateStr",
                        sheetId = sheet.id,
                        date = dateStr,
                        status = "present",
                        basicHours = b,
                        otHours = o,
                        customOtMultiplier = null
                    )
                )
            }
            if (list.isNotEmpty()) {
                repository.saveRecords(list)
            }
        }
    }

    fun addAdvance(date: String, amount: Double, note: String) {
        val sheet = currentSheet.value ?: return
        if (amount <= 0) return
        viewModelScope.launch {
            repository.addAdvance(sheet.id, date, amount, note)
        }
    }

    fun deleteAdvance(id: Long) {
        viewModelScope.launch {
            repository.deleteAdvance(id)
        }
    }

    fun cycleWeeklyOff() {
        val presets = Localization.WEEKLY_OFF_PRESETS
        val currentOff = settings.value.weeklyOffDays
        val currentIndex = presets.indexOfFirst { it.first == currentOff }
        val nextIndex = (currentIndex + 1) % presets.size
        val nextOff = presets[nextIndex].first

        viewModelScope.launch {
            repository.updateSettings { it.copy(weeklyOffDays = nextOff) }
        }
    }

    fun toggleReportEarnings() {
        viewModelScope.launch {
            repository.updateSettings { it.copy(showEarningsInReport = !it.showEarningsInReport) }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(language = lang) }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(themeMode = mode) }
        }
    }

    fun setDefaultOtMultiplier(otx: Double) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(defaultOtMultiplier = otx) }
            val sheet = currentSheet.value
            if (sheet != null) {
                repository.saveSheet(sheet.copy(otMultiplier = otx))
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _selectedSheetId.value = null
            _currentMonth.value = Localization.getCurrentMonthString()
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportCompleteJson(allSheets.value)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return repository.importJson(jsonString)
    }

    fun generatePdfReport(): File? {
        val sheet = currentSheet.value ?: return null
        val data = PdfReportGenerator.ReportData(
            sheet = sheet,
            records = dayRecords.value,
            advances = advances.value,
            settings = settings.value
        )
        return PdfReportGenerator.createPdfReport(getApplication(), data)
    }
}
