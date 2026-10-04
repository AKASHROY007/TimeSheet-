package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TimeSheetEntity
import com.example.ui.components.AdvancesSection
import com.example.ui.components.DailyTableSection
import com.example.ui.components.KpiCardsSection
import com.example.ui.components.MetaSection
import com.example.ui.components.QuickFillDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SummarySection
import com.example.ui.components.TopHeader
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun TimeSheetScreen(
    viewModel: TimeSheetViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val sheets by viewModel.sheetsForMonth.collectAsStateWithLifecycle()
    val currentSheet by viewModel.currentSheet.collectAsStateWithLifecycle()
    val records by viewModel.dayRecords.collectAsStateWithLifecycle()
    val advances by viewModel.advances.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allSheets by viewModel.allSheets.collectAsStateWithLifecycle()
    val allPresentRecords by viewModel.allPresentRecords.collectAsStateWithLifecycle()

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isQuickFillOpen by viewModel.isQuickFillOpen.collectAsStateWithLifecycle()
    val isAddSheetOpen by viewModel.isAddSheetOpen.collectAsStateWithLifecycle()
    val isResetConfirmOpen by viewModel.isResetConfirmOpen.collectAsStateWithLifecycle()
    var sheetPendingDelete by remember { mutableStateOf<TimeSheetEntity?>(null) }

    // Derived summary calculations
    val basicHoursSum by remember(records) {
        derivedStateOf {
            records.filter { it.status == "present" }.sumOf { it.basicHours }
        }
    }
    val otHoursSum by remember(records) {
        derivedStateOf {
            records.filter { it.status == "present" }.sumOf { it.otHours }
        }
    }
    val totalHoursSum by remember(records) {
        derivedStateOf { basicHoursSum + otHoursSum }
    }
    val workingDaysCount by remember(records) {
        derivedStateOf {
            records.count { it.status == "present" && (it.basicHours + it.otHours > 0) }
        }
    }
    val totalEarningsSum by remember(records, currentSheet, settings) {
        derivedStateOf {
            val rate = currentSheet?.hourlyRate ?: 0.0
            val parts = currentMonth.split("-")
            val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 10

            records.filter { it.status == "present" }.sumOf { r ->
                val day = r.date.split("-").getOrNull(2)?.toIntOrNull() ?: 1
                val dow = Localization.getDayOfWeek(y, m, day)
                val isOff = settings.weeklyOffDays.contains(dow)
                val otx = r.customOtMultiplier ?: if (isOff) 2.0 else (currentSheet?.otMultiplier ?: 1.0)
                (r.basicHours + r.otHours * otx) * rate
            }
        }
    }
    val totalAdvancesSum by remember(advances) {
        derivedStateOf { advances.sumOf { it.amount } }
    }

    // Lifetime stats
    val lifetimeHours by remember(allPresentRecords) {
        derivedStateOf {
            allPresentRecords.sumOf { it.basicHours + it.otHours }
        }
    }
    val lifetimeEarningsFormatted by remember(allSheets, allPresentRecords, settings) {
        derivedStateOf {
            val sheetMap = allSheets.associateBy { it.id }
            val earningsByCurrency = mutableMapOf<String, Double>()

            for (r in allPresentRecords) {
                val s = sheetMap[r.sheetId] ?: continue
                val parts = r.date.split("-")
                val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 10
                val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
                val dow = Localization.getDayOfWeek(y, m, d)
                val isOff = settings.weeklyOffDays.contains(dow)
                val otx = r.customOtMultiplier ?: if (isOff) 2.0 else s.otMultiplier
                val earn = (r.basicHours + r.otHours * otx) * s.hourlyRate
                earningsByCurrency[s.currency] = (earningsByCurrency[s.currency] ?: 0.0) + earn
            }

            val validEntries = earningsByCurrency.filter { it.value > 0 }
            if (validEntries.isEmpty()) {
                "—"
            } else {
                validEntries.entries.joinToString(" + ") {
                    Localization.formatMoney(it.value, it.key)
                }
            }
        }
    }

    // File Picker for JSON Restore
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val content = stream?.bufferedReader()?.use { it.readText() } ?: ""
                    val success = viewModel.importBackupJson(content)
                    if (success) {
                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, Localization.t("Invalid backup file.", settings.language), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to restore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun handleBackupShare() {
        scope.launch {
            try {
                val json = viewModel.exportBackupJson()
                val backupFile = File(context.cacheDir, "timesheet_backup_${Localization.getCurrentDateString()}.json")
                FileOutputStream(backupFile).use { it.write(json.toByteArray()) }

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    backupFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, backupFile.name)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Backup JSON"))
            } catch (e: Exception) {
                Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handlePrintOrSharePdf() {
        val pdfFile = viewModel.generatePdfReport()
        if (pdfFile != null) {
            PdfReportGenerator.sharePdf(context, pdfFile)
        } else {
            Toast.makeText(context, "No sheet to export", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // Header
                TopHeader(
                    currentMonth = currentMonth,
                    settings = settings,
                    onPrevMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                    onOpenSettings = { viewModel.isSettingsOpen.value = true }
                )

                // Sheet & Worker metadata
                MetaSection(
                    sheets = sheets,
                    currentSheet = currentSheet,
                    settings = settings,
                    onSelectSheet = { viewModel.selectSheet(it) },
                    onAddSheet = { viewModel.isAddSheetOpen.value = true },
                    onDeleteSheet = { sheetPendingDelete = currentSheet },
                    onShareReport = { handlePrintOrSharePdf() },
                    onUpdateUserProfile = { n, t -> viewModel.updateUserProfile(n, t) },
                    onUpdateSheetMeta = { co, sub -> viewModel.updateSheetMeta(co, sub) }
                )

                // 3 Top Cards (Monthly Hours, Monthly Earnings, Hourly Rate)
                KpiCardsSection(
                    totalHours = totalHoursSum,
                    basicHours = basicHoursSum,
                    otHours = otHoursSum,
                    totalEarnings = totalEarningsSum,
                    totalAdvances = totalAdvancesSum,
                    hourlyRate = currentSheet?.hourlyRate ?: 0.0,
                    currency = currentSheet?.currency ?: "BHD",
                    otMultiplier = currentSheet?.otMultiplier ?: 1.0,
                    isMultiSheet = sheets.size > 1,
                    settings = settings,
                    onUpdateRate = { viewModel.updateSheetRate(it) },
                    onUpdateCurrency = { viewModel.updateSheetCurrency(it) }
                )

                // Daily Breakdown Table
                DailyTableSection(
                    currentMonth = currentMonth,
                    sheet = currentSheet,
                    records = records,
                    settings = settings,
                    onToggleStatus = { viewModel.toggleDayStatus(it) },
                    onUpdateHours = { d, b, o -> viewModel.updateDayHours(d, b, o) },
                    onCycleOtMultiplier = { viewModel.cycleDayOtMultiplier(it) }
                )

                // Summary Cards
                SummarySection(
                    basicHours = basicHoursSum,
                    otHours = otHoursSum,
                    totalHours = totalHoursSum,
                    workingDays = workingDaysCount,
                    lifetimeHours = lifetimeHours,
                    lifetimeEarnings = lifetimeEarningsFormatted,
                    settings = settings
                )

                // Advances Section
                AdvancesSection(
                    advances = advances,
                    totalAdvances = totalAdvancesSum,
                    totalEarnings = totalEarningsSum,
                    currency = currentSheet?.currency ?: "BHD",
                    currentMonth = currentMonth,
                    settings = settings,
                    onAddAdvance = { d, a, n -> viewModel.addAdvance(d, a, n) },
                    onDeleteAdvance = { viewModel.deleteAdvance(it) }
                )
            }
        }
    }

    // Settings Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            settings = settings,
            onDismiss = { viewModel.isSettingsOpen.value = false },
            onToggleTheme = {
                val next = if (settings.themeMode == "dark") "light" else "dark"
                viewModel.setThemeMode(next)
            },
            onSelectLanguage = { viewModel.setLanguage(it) },
            onSelectDefaultOt = { viewModel.setDefaultOtMultiplier(it) },
            onOpenQuickFill = { viewModel.isQuickFillOpen.value = true },
            onCycleWeeklyOff = { viewModel.cycleWeeklyOff() },
            onToggleReportEarnings = { viewModel.toggleReportEarnings() },
            onBackupData = { handleBackupShare() },
            onRestoreData = { restoreFileLauncher.launch("application/json") },
            onResetData = { viewModel.isResetConfirmOpen.value = true }
        )
    }

    // Quick Fill Dialog
    if (isQuickFillOpen) {
        QuickFillDialog(
            currentMonth = currentMonth,
            records = records,
            settings = settings,
            onDismiss = { viewModel.isQuickFillOpen.value = false },
            onFill = { b, o, skipOff, onlyEmpty, upToToday ->
                viewModel.quickFillMonth(b, o, skipOff, onlyEmpty, upToToday)
            }
        )
    }

    // Add Sheet Dialog
    if (isAddSheetOpen) {
        var newCompany by remember { mutableStateOf("") }
        var newSub by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.isAddSheetOpen.value = false },
            title = { Text(Localization.t("Add Sheet", settings.language)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCompany,
                        onValueChange = { newCompany = it },
                        label = { Text(Localization.t("Company", settings.language)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_sheet_company_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSub,
                        onValueChange = { newSub = it },
                        label = { Text(Localization.t("Sub-contractor", settings.language)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_sheet_sub_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewSheet(newCompany, newSub)
                        viewModel.isAddSheetOpen.value = false
                    },
                    modifier = Modifier.testTag("add_sheet_submit_btn")
                ) {
                    Text(Localization.t("Add Sheet", settings.language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.isAddSheetOpen.value = false }) {
                    Text(Localization.t("Cancel", settings.language))
                }
            }
        )
    }

    // Delete Sheet Confirmation Dialog
    sheetPendingDelete?.let { sheet ->
        AlertDialog(
            onDismissRequest = { sheetPendingDelete = null },
            title = { Text(Localization.t("Confirm Delete", settings.language)) },
            text = {
                Text(Localization.t("Are you sure you want to delete this sheet?", settings.language))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentSheet()
                        sheetPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_delete_sheet_btn")
                ) {
                    Text(Localization.t("Delete", settings.language), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { sheetPendingDelete = null }) {
                    Text(Localization.t("Cancel", settings.language))
                }
            }
        )
    }

    // Reset Data Confirmation Dialog
    if (isResetConfirmOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.isResetConfirmOpen.value = false },
            title = { Text(Localization.t("Reset Data", settings.language)) },
            text = {
                Text(Localization.t("Are you sure you want to reset all data?", settings.language))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        viewModel.isResetConfirmOpen.value = false
                        viewModel.isSettingsOpen.value = false
                        Toast.makeText(context, "All data reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_reset_data_btn")
                ) {
                    Text(Localization.t("Delete", settings.language), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.isResetConfirmOpen.value = false }) {
                    Text(Localization.t("Cancel", settings.language))
                }
            }
        )
    }
}
