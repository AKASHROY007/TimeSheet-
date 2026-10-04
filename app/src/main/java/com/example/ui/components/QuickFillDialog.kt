package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.DayRecordEntity
import com.example.util.Localization
import java.util.Locale

@Composable
fun QuickFillDialog(
    currentMonth: String,
    records: List<DayRecordEntity>,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onFill: (basic: Double, ot: Double, skipWeeklyOff: Boolean, onlyEmpty: Boolean, upToToday: Boolean) -> Unit
) {
    var basicText by remember { mutableStateOf("8") }
    var otText by remember { mutableStateOf("0") }
    var skipWeeklyOff by remember { mutableStateOf(true) }
    var onlyEmpty by remember { mutableStateOf(true) }
    var upToToday by remember {
        mutableStateOf(currentMonth == Localization.getCurrentMonthString())
    }

    val parts = currentMonth.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val totalDays = Localization.getDaysInMonth(year, month)
    val todayDate = Localization.getCurrentDateString()

    val recordsMap = remember(records) { records.associateBy { it.date } }

    val calculatedMatchingDays by remember(skipWeeklyOff, onlyEmpty, upToToday, records) {
        derivedStateOf {
            var count = 0
            for (d in 1..totalDays) {
                val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
                if (upToToday && dateStr > todayDate) continue
                val dow = Localization.getDayOfWeek(year, month, d)
                if (skipWeeklyOff && settings.weeklyOffDays.contains(dow)) continue
                val rec = recordsMap[dateStr]
                if (onlyEmpty && rec != null && rec.status.isNotBlank()) continue
                count++
            }
            count
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Localization.t("Quick Fill Month", settings.language),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = basicText,
                    onValueChange = { basicText = it.replace(',', '.') },
                    label = { Text(Localization.t("Basic hours", settings.language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("qf_basic_input")
                )

                OutlinedTextField(
                    value = otText,
                    onValueChange = { otText = it.replace(',', '.') },
                    label = { Text(Localization.t("OT hours", settings.language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("qf_ot_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = skipWeeklyOff,
                        onCheckedChange = { skipWeeklyOff = it }
                    )
                    Text(
                        text = Localization.t("Skip weekly off days", settings.language),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = onlyEmpty,
                        onCheckedChange = { onlyEmpty = it }
                    )
                    Text(
                        text = Localization.t("Only fill empty days", settings.language),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = upToToday,
                        onCheckedChange = { upToToday = it }
                    )
                    Text(
                        text = Localization.t("Up to today only", settings.language),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val b = basicText.toDoubleOrNull() ?: 8.0
                    val o = otText.toDoubleOrNull() ?: 0.0
                    onFill(b, o, skipWeeklyOff, onlyEmpty, upToToday)
                    onDismiss()
                },
                enabled = calculatedMatchingDays > 0,
                modifier = Modifier.testTag("qf_confirm_btn")
            ) {
                Text("${Localization.t("Fill", settings.language)} ($calculatedMatchingDays ${Localization.t("days", settings.language)})")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(Localization.t("Cancel", settings.language))
            }
        }
    )
}
