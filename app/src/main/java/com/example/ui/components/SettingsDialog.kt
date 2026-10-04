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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppSettings
import com.example.util.Localization

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectDefaultOt: (Double) -> Unit,
    onOpenQuickFill: () -> Unit,
    onCycleWeeklyOff: () -> Unit,
    onToggleReportEarnings: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onResetData: () -> Unit
) {
    var langMenuExpanded by remember { mutableStateOf(false) }
    var otMenuExpanded by remember { mutableStateOf(false) }
    var showChangelog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Localization.t("Settings", settings.language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.t("Close", settings.language),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section: Appearance
                SettingsSectionTitle(Localization.t("Appearance", settings.language))
                val isDark = settings.themeMode == "dark"
                SettingsButton(
                    icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconTint = if (isDark) Color(0xFFFBBF24) else Color(0xFF6366F1),
                    title = if (isDark) Localization.t("Dark mode", settings.language) else Localization.t("Light mode", settings.language),
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { onToggleTheme() },
                            modifier = Modifier.testTag("theme_switch")
                        )
                    },
                    onClick = onToggleTheme
                )

                // Section: Language
                SettingsSectionTitle(Localization.t("Language", settings.language))
                Box {
                    val langTitle = when (settings.language) {
                        "bn" -> "বাংলা"
                        "ar" -> "العربية"
                        else -> "English"
                    }
                    SettingsButton(
                        icon = Icons.Default.Language,
                        iconTint = Color(0xFFA855F7),
                        title = Localization.t("Language", settings.language),
                        trailing = {
                            Text(langTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        },
                        onClick = { langMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = langMenuExpanded,
                        onDismissRequest = { langMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                onSelectLanguage("en")
                                langMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("বাংলা (Bangla)") },
                            onClick = {
                                onSelectLanguage("bn")
                                langMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("العربية (Arabic)") },
                            onClick = {
                                onSelectLanguage("ar")
                                langMenuExpanded = false
                            }
                        )
                    }
                }

                // Section: Overtime
                SettingsSectionTitle(Localization.t("Overtime", settings.language))
                Box {
                    SettingsButton(
                        icon = Icons.Default.Schedule,
                        iconTint = Color(0xFFEC4899),
                        title = Localization.t("Normal day OT rate", settings.language),
                        trailing = {
                            Text("OT ×${settings.defaultOtMultiplier}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        },
                        onClick = { otMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = otMenuExpanded,
                        onDismissRequest = { otMenuExpanded = false }
                    ) {
                        listOf(1.0, 1.25, 1.5, 2.0).forEach { rate ->
                            DropdownMenuItem(
                                text = { Text("OT ×$rate") },
                                onClick = {
                                    onSelectDefaultOt(rate)
                                    otMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Section: Timesheet Tools
                SettingsSectionTitle(Localization.t("Timesheet", settings.language))
                SettingsButton(
                    icon = Icons.Default.Today,
                    iconTint = Color(0xFF6366F1),
                    title = Localization.t("Quick Fill Month", settings.language),
                    onClick = {
                        onDismiss()
                        onOpenQuickFill()
                    }
                )

                val weeklyOffLabel = Localization.t("Weekly Off", settings.language) + ": " + when {
                    settings.weeklyOffDays.isEmpty() -> Localization.t("None", settings.language)
                    settings.weeklyOffDays == listOf(5) -> Localization.t("Friday", settings.language)
                    settings.weeklyOffDays == listOf(6) -> Localization.t("Saturday", settings.language)
                    settings.weeklyOffDays == listOf(0) -> Localization.t("Sunday", settings.language)
                    settings.weeklyOffDays == listOf(5, 6) -> Localization.t("Friday + Saturday", settings.language)
                    else -> settings.weeklyOffDays.joinToString("+")
                }
                SettingsButton(
                    icon = Icons.Default.Schedule,
                    iconTint = Color(0xFFF97316),
                    title = weeklyOffLabel,
                    onClick = onCycleWeeklyOff
                )

                val earningsReportStatus = if (settings.showEarningsInReport) Localization.t("ON", settings.language) else Localization.t("OFF", settings.language)
                SettingsButton(
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFF10B981),
                    title = "${Localization.t("Earnings in Report", settings.language)}: $earningsReportStatus",
                    onClick = onToggleReportEarnings
                )

                // Section: Data
                SettingsSectionTitle(Localization.t("Data", settings.language))
                SettingsButton(
                    icon = Icons.Default.FileDownload,
                    iconTint = Color(0xFF0EA5E9),
                    title = Localization.t("Backup Data", settings.language),
                    onClick = onBackupData
                )

                SettingsButton(
                    icon = Icons.Default.FileUpload,
                    iconTint = Color(0xFF14B8A6),
                    title = Localization.t("Restore Data", settings.language),
                    onClick = onRestoreData
                )

                SettingsButton(
                    icon = Icons.Default.Refresh,
                    iconTint = Color(0xFFEF4444),
                    title = Localization.t("Reset Data", settings.language),
                    textColor = Color(0xFFEF4444),
                    onClick = onResetData
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Version string
                Text(
                    text = "Time Sheet • Version 2.1.17",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showChangelog = true }
                        .padding(4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showChangelog) {
        AlertDialog(
            onDismissRequest = { showChangelog = false },
            title = { Text("Time Sheet v2.1.17") },
            text = {
                Text(
                    "• Full Android native conversion\n" +
                    "• Room database local persistence\n" +
                    "• Multiple sheets per month\n" +
                    "• Overtime multipliers, advances, earnings\n" +
                    "• PDF report generation and native sharing\n" +
                    "• Multilingual (English, বাংলা, العربية)\n" +
                    "• Full backup & restore JSON support",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(onClick = { showChangelog = false }) {
                    Text(Localization.t("Close", settings.language))
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsButton(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            trailing?.invoke()
        }
    }
}
