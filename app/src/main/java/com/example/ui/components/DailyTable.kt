package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DayRecordEntity
import com.example.data.model.TimeSheetEntity
import com.example.util.Localization
import java.util.Locale

@Composable
fun DailyTableSection(
    currentMonth: String,
    sheet: TimeSheetEntity?,
    records: List<DayRecordEntity>,
    settings: AppSettings,
    onToggleStatus: (String) -> Unit,
    onUpdateHours: (date: String, basic: Double, ot: Double) -> Unit,
    onCycleOtMultiplier: (String) -> Unit
) {
    val parts = currentMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val daysCount = Localization.getDaysInMonth(year, month)
    val todayDate = Localization.getCurrentDateString()

    val recordsMap = remember(records) {
        records.associateBy { it.date }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // Table Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.t("Daily Breakdown", settings.language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Table Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.t("Status", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(44.dp)
                )
                Text(
                    text = Localization.t("Date", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = Localization.t("Basic", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Localization.t("OT", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = Localization.t("Total", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Localization.t("Earnings", settings.language).uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.3f)
                )
            }

            // Rows
            Column {
                for (day in 1..daysCount) {
                    val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                    val dow = Localization.getDayOfWeek(year, month, day)
                    val dowName = Localization.getDayOfWeekName(dow, settings.language)
                    val isWeeklyOff = settings.weeklyOffDays.contains(dow)
                    val record = recordsMap[dateKey]
                    val isToday = dateKey == todayDate

                    DailyRowItem(
                        day = day,
                        dowName = dowName,
                        dateKey = dateKey,
                        isToday = isToday,
                        isWeeklyOff = isWeeklyOff,
                        record = record,
                        sheet = sheet,
                        settings = settings,
                        onToggleStatus = { onToggleStatus(dateKey) },
                        onUpdateHours = { b, o -> onUpdateHours(dateKey, b, o) },
                        onCycleOtMultiplier = { onCycleOtMultiplier(dateKey) }
                    )
                }
            }
        }
    }

    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(Color(0xFF334155), Localization.t("Not entered", settings.language))
        Spacer(modifier = Modifier.width(10.dp))
        LegendDot(Color(0xFF059669), Localization.t("Present", settings.language))
        Spacer(modifier = Modifier.width(10.dp))
        LegendDot(Color(0xFFDC2626), Localization.t("Absent", settings.language))
        Spacer(modifier = Modifier.width(10.dp))
        LegendDot(Color(0xFFD97706), Localization.t("Holiday / Off", settings.language))
    }
}

@Composable
fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = text, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DailyRowItem(
    day: Int,
    dowName: String,
    dateKey: String,
    isToday: Boolean,
    isWeeklyOff: Boolean,
    record: DayRecordEntity?,
    sheet: TimeSheetEntity?,
    settings: AppSettings,
    onToggleStatus: () -> Unit,
    onUpdateHours: (Double, Double) -> Unit,
    onCycleOtMultiplier: () -> Unit
) {
    val status = record?.status ?: ""
    val isPresent = status == "present"
    val isAbsent = status == "absent"
    val isHoliday = status == "holiday"

    val rate = sheet?.hourlyRate ?: 0.0
    val defaultOtx = if (isWeeklyOff) 2.0 else (sheet?.otMultiplier ?: 1.0)
    val effectiveOtx = record?.customOtMultiplier ?: defaultOtx

    val basicHours = if (isPresent) (record?.basicHours ?: 8.0) else 0.0
    val otHours = if (isPresent) (record?.otHours ?: 0.0) else 0.0
    val totalHours = basicHours + otHours
    val dailyEarnings = if (isPresent) (basicHours + (otHours * effectiveOtx)) * rate else 0.0

    val rowBg = if (isToday) Color(0xFF6366F1).copy(alpha = 0.08f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
            .padding(vertical = 3.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status button
        Box(
            modifier = Modifier.width(44.dp),
            contentAlignment = Alignment.Center
        ) {
            val statusColor = when {
                isPresent -> Color(0xFF059669)
                isAbsent -> Color(0xFFDC2626)
                isHoliday -> Color(0xFFD97706)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
            val statusSymbol = when {
                isPresent -> "✓"
                isAbsent -> "−"
                isHoliday -> "★"
                else -> ""
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor)
                    .clickable { onToggleStatus() }
                    .testTag("status_btn_$dateKey"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusSymbol,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }

        // Date & Day
        val dateColor = when {
            isPresent -> Color(0xFF10B981)
            isAbsent -> Color(0xFFF87171)
            isHoliday -> Color(0xFFFBBF24)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Text(
            text = String.format(Locale.US, "%02d %s", day, dowName),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = dateColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.2f)
        )

        // Basic Hours Input
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (isPresent) {
                var basicText by remember(record?.basicHours) {
                    mutableStateOf(String.format(Locale.US, "%.2f", basicHours).removeSuffix(".00"))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(26.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    BasicTextField(
                        value = basicText,
                        onValueChange = { input ->
                            val clean = input.replace(',', '.')
                            basicText = clean
                            val parsed = clean.toDoubleOrNull()
                            if (parsed != null) {
                                onUpdateHours(parsed, otHours)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.padding(top = 4.dp).testTag("basic_input_$dateKey")
                    )
                }
            } else {
                Text(if (isAbsent || isHoliday) "—" else "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // OT Hours Input + OT multiplier badge
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
            if (isPresent) {
                var otText by remember(record?.otHours) {
                    mutableStateOf(String.format(Locale.US, "%.2f", otHours).removeSuffix(".00"))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(26.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = otText,
                            onValueChange = { input ->
                                val clean = input.replace(',', '.')
                                otText = clean
                                val parsed = clean.toDoubleOrNull()
                                if (parsed != null) {
                                    onUpdateHours(basicHours, parsed)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).padding(top = 2.dp).testTag("ot_input_$dateKey")
                        )

                        Text(
                            text = "×$effectiveOtx",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (record?.customOtMultiplier != null) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .clickable { onCycleOtMultiplier() }
                                .padding(horizontal = 1.dp)
                                .testTag("ot_mult_btn_$dateKey")
                        )
                    }
                }
            } else {
                Text(if (isAbsent || isHoliday) "—" else "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Total
        Text(
            text = if (isPresent) Localization.formatHours(totalHours, false) else if (isAbsent || isHoliday) "—" else "",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = if (isPresent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        // Earnings
        Text(
            text = if (isPresent) Localization.formatMoney(dailyEarnings, sheet?.currency ?: "BHD") else if (isAbsent || isHoliday) "—" else "",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPresent) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.3f)
        )
    }
}
