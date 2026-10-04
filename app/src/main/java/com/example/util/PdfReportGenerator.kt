package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.data.model.AdvanceEntity
import com.example.data.model.AppSettings
import com.example.data.model.DayRecordEntity
import com.example.data.model.TimeSheetEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    data class ReportData(
        val sheet: TimeSheetEntity,
        val records: List<DayRecordEntity>,
        val advances: List<AdvanceEntity>,
        val settings: AppSettings
    )

    fun createPdfReport(context: Context, data: ReportData): File {
        val parts = data.sheet.month.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 10
        val monthName = Localization.MONTHS_EN.getOrNull(month - 1) ?: "Month"

        val daysCount = Localization.getDaysInMonth(year, month)
        val recMap = data.records.associateBy { it.date }

        val pageHeight = 1100
        val pageWidth = 650
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        canvas.drawColor(Color.WHITE)

        // Header Title
        paint.color = Color.parseColor("#4338CA")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Time Sheet", 30f, 40f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Duty & overtime report", 30f, 55f, paint)

        // Month / Year right aligned
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        val dateText = "$monthName $year"
        val dateWidth = paint.measureText(dateText)
        canvas.drawText(dateText, pageWidth - 30f - dateWidth, 40f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        paint.isFakeBoldText = false
        val subLabel = "MONTH / YEAR"
        canvas.drawText(subLabel, pageWidth - 30f - paint.measureText(subLabel), 54f, paint)

        // Dividing line
        paint.color = Color.parseColor("#4F46E5")
        paint.strokeWidth = 2f
        canvas.drawLine(30f, 65f, pageWidth - 30f, 65f, paint)

        // Employee Info Box (2x2 grid)
        val infoY = 75f
        val boxWidth = (pageWidth - 65f) / 2f
        val boxHeight = 28f

        fun drawInfoCell(x: Float, y: Float, label: String, value: String) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 1f
            canvas.drawRect(x, y, x + boxWidth, y + boxHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 7f
            canvas.drawText(label.uppercase(Locale.US), x + 6f, y + 10f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            canvas.drawText(value.ifBlank { "—" }, x + 6f, y + 23f, paint)
        }

        drawInfoCell(30f, infoY, "Name", data.settings.userName)
        drawInfoCell(35f + boxWidth, infoY, "Trade", data.settings.trade)
        drawInfoCell(30f, infoY + boxHeight + 4f, "Company", data.sheet.company)
        drawInfoCell(35f + boxWidth, infoY + boxHeight + 4f, "Sub-contractor", data.sheet.subContractor)

        // KPI Box Summary
        var totalHours = 0.0
        var totalBasic = 0.0
        var totalOt = 0.0
        var workingDays = 0
        var absentDays = 0
        var totalEarnings = 0.0

        for (d in 1..daysCount) {
            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
            val r = recMap[dateKey]
            if (r?.status == "present") {
                val b = r.basicHours
                val o = r.otHours
                totalBasic += b
                totalOt += o
                totalHours += (b + o)
                if (b + o > 0) workingDays++

                val dow = Localization.getDayOfWeek(year, month, d)
                val isOff = data.settings.weeklyOffDays.contains(dow)
                val otx = r.customOtMultiplier ?: if (isOff) 2.0 else data.sheet.otMultiplier
                totalEarnings += (b + o * otx) * data.sheet.hourlyRate
            } else if (r?.status == "absent") {
                absentDays++
            }
        }

        val totalAdvances = data.advances.sumOf { it.amount }

        val kpiY = infoY + (boxHeight * 2) + 12f
        drawInfoCell(30f, kpiY, "Total Hours", Localization.formatHours(totalHours))
        drawInfoCell(35f + boxWidth, kpiY, "Absent Days", "$absentDays days")
        drawInfoCell(
            30f,
            kpiY + boxHeight + 4f,
            "Total Amount",
            if (data.settings.showEarningsInReport && data.sheet.hourlyRate > 0) Localization.formatMoney(totalEarnings, data.sheet.currency) else "—"
        )
        drawInfoCell(
            35f + boxWidth,
            kpiY + boxHeight + 4f,
            "Advances",
            if (totalAdvances > 0) Localization.formatMoney(totalAdvances, data.sheet.currency) else "—"
        )

        // Table Header
        val tableY = kpiY + (boxHeight * 2) + 16f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Daily Breakdown", 30f, tableY - 4f, paint)

        val colStatusW = 55f
        val colDateW = 85f
        val colBasicW = 60f
        val colOtW = 60f
        val colTotalW = 70f
        val colEarnW = if (data.settings.showEarningsInReport) 90f else 0f
        val tableW = colStatusW + colDateW + colBasicW + colOtW + colTotalW + colEarnW

        val thY = tableY
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, thY, 30f + tableW, thY + 16f, paint)

        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        canvas.drawRect(30f, thY, 30f + tableW, thY + 16f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 7.5f
        paint.isFakeBoldText = true

        var colX = 30f
        fun drawThText(text: String, width: Float) {
            val textW = paint.measureText(text)
            canvas.drawText(text, colX + (width - textW) / 2f, thY + 11f, paint)
            colX += width
        }

        drawThText("STATUS", colStatusW)
        drawThText("DATE", colDateW)
        drawThText("BASIC", colBasicW)
        drawThText("OT", colOtW)
        drawThText("TOTAL", colTotalW)
        if (data.settings.showEarningsInReport) {
            drawThText("EARNINGS", colEarnW)
        }

        // Table Rows
        var rowY = thY + 16f
        val rowH = 17.5f

        for (d in 1..daysCount) {
            val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
            val dow = Localization.getDayOfWeek(year, month, d)
            val dowName = Localization.DAYS_OF_WEEK_EN[dow]
            val r = recMap[dateKey]
            val status = r?.status ?: ""
            val isPresent = status == "present"

            // Row bottom line
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawLine(30f, rowY + rowH, 30f + tableW, rowY + rowH, paint)

            // Status badge
            val badgeX = 30f + (colStatusW - 14f) / 2f
            val badgeY = rowY + (rowH - 14f) / 2f
            val rect = RectF(badgeX, badgeY, badgeX + 14f, badgeY + 14f)

            paint.style = Paint.Style.FILL
            when (status) {
                "present" -> {
                    paint.color = Color.parseColor("#D1FAE5")
                    canvas.drawRoundRect(rect, 3f, 3f, paint)
                    paint.color = Color.parseColor("#047857")
                    paint.textSize = 10f
                    paint.isFakeBoldText = true
                    canvas.drawText("✓", badgeX + 3f, badgeY + 11f, paint)
                }
                "absent" -> {
                    paint.color = Color.parseColor("#FEE2E2")
                    canvas.drawRoundRect(rect, 3f, 3f, paint)
                    paint.color = Color.parseColor("#B91C1C")
                    paint.textSize = 11f
                    paint.isFakeBoldText = true
                    canvas.drawText("−", badgeX + 3.5f, badgeY + 10.5f, paint)
                }
                "holiday" -> {
                    paint.color = Color.parseColor("#FEF3C7")
                    canvas.drawRoundRect(rect, 3f, 3f, paint)
                    paint.color = Color.parseColor("#B45309")
                    paint.textSize = 10f
                    paint.isFakeBoldText = true
                    canvas.drawText("★", badgeX + 2.5f, badgeY + 11f, paint)
                }
                else -> {
                    paint.color = Color.parseColor("#F1F5F9")
                    canvas.drawRoundRect(rect, 3f, 3f, paint)
                }
            }

            // Date text
            val dateStr = String.format(Locale.US, "%02d %s", d, dowName)
            paint.color = if (isPresent) Color.parseColor("#047857") else Color.parseColor("#64748B")
            paint.textSize = 8.5f
            paint.isFakeBoldText = true
            val dWidth = paint.measureText(dateStr)
            canvas.drawText(dateStr, 30f + colStatusW + (colDateW - dWidth) / 2f, rowY + 12f, paint)

            // Basic
            val basicStr = if (isPresent) String.format(Locale.US, "%.2f", r?.basicHours ?: 0.0) else "—"
            paint.color = Color.parseColor("#0F172A")
            paint.isFakeBoldText = false
            var curX = 30f + colStatusW + colDateW
            canvas.drawText(basicStr, curX + (colBasicW - paint.measureText(basicStr)) / 2f, rowY + 12f, paint)

            // OT
            curX += colBasicW
            val otStr = if (isPresent) String.format(Locale.US, "%.2f", r?.otHours ?: 0.0) else "—"
            canvas.drawText(otStr, curX + (colOtW - paint.measureText(otStr)) / 2f, rowY + 12f, paint)

            // Total
            curX += colOtW
            val totStr = if (isPresent) String.format(Locale.US, "%.2f", (r?.basicHours ?: 0.0) + (r?.otHours ?: 0.0)) else "—"
            paint.isFakeBoldText = isPresent
            canvas.drawText(totStr, curX + (colTotalW - paint.measureText(totStr)) / 2f, rowY + 12f, paint)

            // Earnings
            if (data.settings.showEarningsInReport) {
                curX += colTotalW
                val earnStr = if (isPresent) {
                    val isOff = data.settings.weeklyOffDays.contains(dow)
                    val otx = r?.customOtMultiplier ?: if (isOff) 2.0 else data.sheet.otMultiplier
                    val amt = ((r?.basicHours ?: 0.0) + (r?.otHours ?: 0.0) * otx) * data.sheet.hourlyRate
                    Localization.formatMoney(amt, data.sheet.currency)
                } else "—"
                paint.color = Color.parseColor("#047857")
                canvas.drawText(earnStr, curX + (colEarnW - paint.measureText(earnStr)) / 2f, rowY + 12f, paint)
            }

            rowY += rowH
        }

        // Table Footer (Total row)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, rowY, 30f + tableW, rowY + 20f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        val totalLabel = "TOTAL ($workingDays days)"
        canvas.drawText(totalLabel, 36f, rowY + 14f, paint)

        var footX = 30f + colStatusW + colDateW
        val bSum = String.format(Locale.US, "%.2f", totalBasic)
        canvas.drawText(bSum, footX + (colBasicW - paint.measureText(bSum)) / 2f, rowY + 14f, paint)

        footX += colBasicW
        val oSum = String.format(Locale.US, "%.2f", totalOt)
        canvas.drawText(oSum, footX + (colOtW - paint.measureText(oSum)) / 2f, rowY + 14f, paint)

        footX += colOtW
        val tSum = String.format(Locale.US, "%.2f", totalHours)
        canvas.drawText(tSum, footX + (colTotalW - paint.measureText(tSum)) / 2f, rowY + 14f, paint)

        if (data.settings.showEarningsInReport) {
            footX += colTotalW
            val eSum = Localization.formatMoney(totalEarnings, data.sheet.currency)
            paint.color = Color.parseColor("#047857")
            canvas.drawText(eSum, footX + (colEarnW - paint.measureText(eSum)) / 2f, rowY + 14f, paint)
        }

        // Timestamp Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        val genTime = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date())
        val genText = "Generated on $genTime • Time Sheet App"
        canvas.drawText(genText, 30f, rowY + 36f, paint)

        doc.finishPage(page)

        // Save PDF file
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = "TimeSheet_${data.settings.userName.ifBlank { "Worker" }}_${monthName}_$year.pdf"
            .replace("[\\s\\\\/:*?\"<>|]+".toRegex(), "_")
        val pdfFile = File(reportsDir, fileName)
        FileOutputStream(pdfFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return pdfFile
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, pdfFile.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Time Sheet Report"))
    }
}
