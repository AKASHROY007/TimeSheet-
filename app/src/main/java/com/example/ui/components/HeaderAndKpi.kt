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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.TimeSheetEntity
import com.example.util.Localization

@Composable
fun TopHeader(
    currentMonth: String,
    settings: AppSettings,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val parts = currentMonth.split("-")
    val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 10
    val monthName = Localization.MONTHS_EN.getOrNull(m - 1) ?: "Month"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Localization.t("Time Sheet", settings.language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
                Text(
                    text = Localization.t("Duty & overtime tracker", settings.language),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Month selector & Settings button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(28.dp).testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$monthName $y",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(28.dp).testTag("next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = Localization.t("Settings", settings.language),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MetaSection(
    sheets: List<TimeSheetEntity>,
    currentSheet: TimeSheetEntity?,
    settings: AppSettings,
    onSelectSheet: (String) -> Unit,
    onAddSheet: () -> Unit,
    onDeleteSheet: () -> Unit,
    onShareReport: () -> Unit,
    onUpdateUserProfile: (name: String, trade: String) -> Unit,
    onUpdateSheetMeta: (company: String, sub: String) -> Unit
) {
    var sheetMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        // Sheet selection bar & action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { sheetMenuExpanded = true }
                        .testTag("sheet_dropdown"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sheetLabel = listOfNotNull(
                            currentSheet?.company?.takeIf { it.isNotBlank() },
                            currentSheet?.subContractor?.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank {
                            "${Localization.t("Sheet", settings.language)} ${(sheets.indexOf(currentSheet).takeIf { it >= 0 } ?: 0) + 1}"
                        }

                        Text(
                            text = sheetLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                DropdownMenu(
                    expanded = sheetMenuExpanded,
                    onDismissRequest = { sheetMenuExpanded = false }
                ) {
                    sheets.forEachIndexed { index, s ->
                        val itemLabel = listOfNotNull(
                            s.company.takeIf { it.isNotBlank() },
                            s.subContractor.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank {
                            "${Localization.t("Sheet", settings.language)} ${index + 1}"
                        }
                        DropdownMenuItem(
                            text = { Text("${index + 1}. $itemLabel", fontSize = 13.sp) },
                            onClick = {
                                onSelectSheet(s.id)
                                sheetMenuExpanded = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onAddSheet,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("add_sheet_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Localization.t("Add Sheet", settings.language),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDeleteSheet,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        Color(0xFFEF4444).copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("delete_sheet_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = Localization.t("Delete Sheet", settings.language),
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onShareReport,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4F46E5))
                    .testTag("share_report_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = Localization.t("Share Report", settings.language),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Worker name & Trade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = settings.userName,
                onValueChange = { onUpdateUserProfile(it, settings.trade) },
                placeholder = { Text(Localization.t("Your name", settings.language), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = settings.trade,
                onValueChange = { onUpdateUserProfile(settings.userName, it) },
                placeholder = { Text(Localization.t("Trade", settings.language), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Company & Sub-contractor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = currentSheet?.company ?: "",
                onValueChange = { onUpdateSheetMeta(it, currentSheet?.subContractor ?: "") },
                placeholder = { Text(Localization.t("Company", settings.language), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = currentSheet?.subContractor ?: "",
                onValueChange = { onUpdateSheetMeta(currentSheet?.company ?: "", it) },
                placeholder = { Text(Localization.t("Sub-contractor", settings.language), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun KpiCardsSection(
    totalHours: Double,
    basicHours: Double,
    otHours: Double,
    totalEarnings: Double,
    totalAdvances: Double,
    hourlyRate: Double,
    currency: String,
    otMultiplier: Double,
    isMultiSheet: Boolean,
    settings: AppSettings,
    onUpdateRate: (Double) -> Unit,
    onUpdateCurrency: (String) -> Unit
) {
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Card 1: Monthly / Sheet Hours
        Surface(
            modifier = Modifier.weight(1f).height(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF4F46E5)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Localization.t(if (isMultiSheet) "Sheet Hours" else "Monthly Hours", settings.language).uppercase(),
                    color = Color(0xFFE0E7FF),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Localization.formatHours(totalHours),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "B: ${Localization.formatHours(basicHours, false)} | O: ${Localization.formatHours(otHours, false)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Card 2: Monthly / Sheet Earnings
        Surface(
            modifier = Modifier.weight(1f).height(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF047857)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = Localization.t(if (isMultiSheet) "Sheet Earnings" else "Monthly Earnings", settings.language).uppercase(),
                    color = Color(0xFFD1FAE5),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Localization.formatMoney(totalEarnings, currency),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${Localization.t("Advance", settings.language)}: ${Localization.formatMoney(totalAdvances, currency)}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Card 3: Hourly Rate & Currency
        Surface(
            modifier = Modifier.weight(1f).height(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.t("Hourly Rate", settings.language).uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box {
                        Text(
                            text = currency,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { currencyMenuExpanded = true }
                                .padding(horizontal = 2.dp)
                        )
                        DropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false }
                        ) {
                            Localization.CURRENCIES.forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur, fontSize = 12.sp) },
                                    onClick = {
                                        onUpdateCurrency(cur)
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                var rateText by remember(hourlyRate) {
                    mutableStateOf(if (hourlyRate > 0) hourlyRate.toString() else "")
                }

                androidx.compose.foundation.text.BasicTextField(
                    value = rateText,
                    onValueChange = { input ->
                        val clean = input.replace(',', '.')
                        rateText = clean
                        val parsed = clean.toDoubleOrNull()
                        if (parsed != null) {
                            onUpdateRate(parsed)
                        } else if (clean.isBlank()) {
                            onUpdateRate(0.0)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (rateText.isEmpty()) {
                            Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        innerTextField()
                    }
                )

                val otRate = hourlyRate * otMultiplier
                Text(
                    text = "OT: ${Localization.formatMoney(otRate, currency)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
