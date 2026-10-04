package com.example.util

import java.util.Calendar
import java.util.Locale

object Localization {

    val CURRENCIES = listOf("BHD", "BDT", "USD", "SAR", "AED", "QAR", "OMR", "KWD", "EUR", "INR")

    val CURRENCY_SYMBOLS = mapOf(
        "BHD" to "BHD",
        "BDT" to "৳",
        "USD" to "$",
        "SAR" to "﷼",
        "AED" to "AED",
        "QAR" to "QAR",
        "OMR" to "OMR",
        "KWD" to "KWD",
        "EUR" to "€",
        "INR" to "₹"
    )

    val CURRENCY_DECIMALS = mapOf(
        "BHD" to 3,
        "BDT" to 2,
        "USD" to 2,
        "SAR" to 2,
        "AED" to 2,
        "QAR" to 2,
        "OMR" to 3,
        "KWD" to 3,
        "EUR" to 2,
        "INR" to 2
    )

    val OT_RATES = listOf(1.0, 1.25, 1.5, 2.0, 2.5, 3.0)

    val DAYS_OF_WEEK_EN = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val DAYS_OF_WEEK_BN = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি")
    val DAYS_OF_WEEK_AR = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

    val MONTHS_EN = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val WEEKLY_OFF_PRESETS = listOf(
        emptyList<Int>() to "None",
        listOf(5) to "Friday",
        listOf(6) to "Saturday",
        listOf(0) to "Sunday",
        listOf(5, 6) to "Friday + Saturday"
    )

    private val STRINGS_BN = mapOf(
        "Time Sheet" to "টাইম শিট",
        "Duty & overtime tracker" to "ডিউটি ও ওভারটাইম ট্র্যাকার",
        "Monthly Hours" to "মাসিক ঘণ্টা",
        "Sheet Hours" to "শিটের ঘণ্টা",
        "Monthly Earnings" to "মাসিক আয়",
        "Sheet Earnings" to "শিটের আয়",
        "Hourly Rate" to "ঘণ্টা প্রতি রেট",
        "Daily Breakdown" to "দৈনিক বিবরণ",
        "Status" to "স্ট্যাটাস",
        "Date" to "তারিখ",
        "Basic" to "বেসিক",
        "OT" to "ওটি",
        "Total" to "মোট",
        "Earnings" to "আয়",
        "Not entered" to "এন্ট্রি নেই",
        "Present" to "উপস্থিত",
        "Absent" to "অনুপস্থিত",
        "Holiday / Off" to "ছুটি / অফ",
        "Basic Hours" to "বেসিক ঘণ্টা",
        "Overtime Hours" to "ওভারটাইম ঘণ্টা",
        "Total Hours" to "মোট ঘণ্টা",
        "Working Days" to "কাজের দিন",
        "Lifetime Earnings" to "সর্বমোট আয়",
        "Lifetime Hours" to "সর্বমোট ঘণ্টা",
        "Advances" to "অগ্রিম",
        "+ Add Advance" to "+ অগ্রিম যোগ করুন",
        "Net Payable (Earnings − Advances)" to "নেট প্রদেয় (আয় − অগ্রিম)",
        "Settings" to "সেটিংস",
        "Appearance" to "চেহারা",
        "Dark mode" to "ডার্ক মোড",
        "Light mode" to "লাইট মোড",
        "Language" to "ভাষা",
        "Overtime" to "ওভারটাইম",
        "Normal day OT rate" to "সাধারণ দিনের OT রেট",
        "Timesheet" to "টাইম শিট",
        "Quick Fill Month" to "মাস দ্রুত পূরণ",
        "Data" to "ডেটা",
        "Backup Data" to "ডেটা ব্যাকআপ",
        "Restore Data" to "ডেটা রিস্টোর",
        "Reset Data" to "ডেটা রিসেট",
        "Basic hours" to "বেসিক ঘণ্টা",
        "OT hours" to "OT ঘণ্টা",
        "Skip weekly off days" to "সাপ্তাহিক ছুটি বাদ দিন",
        "Only fill empty days" to "শুধু ফাঁকা দিন পূরণ করুন",
        "Up to today only" to "শুধু আজ পর্যন্ত",
        "Cancel" to "বাতিল",
        "Your name" to "আপনার নাম",
        "Trade" to "পেশা",
        "Company" to "কোম্পানি",
        "Sub-contractor" to "সাব-কন্ট্রাক্টর",
        "Amount" to "পরিমাণ",
        "Note (optional)" to "নোট (ঐচ্ছিক)",
        "days" to "দিন",
        "Advance" to "অগ্রিম",
        "Weekly Off" to "সাপ্তাহিক ছুটি",
        "None" to "নেই",
        "Friday" to "শুক্রবার",
        "Saturday" to "শনিবার",
        "Sunday" to "রবিবার",
        "Friday + Saturday" to "শুক্র + শনি",
        "Earnings in Report" to "রিপোর্টে আয়",
        "ON" to "চালু",
        "OFF" to "বন্ধ",
        "Fill" to "পূরণ করুন",
        "Sheet" to "শিট",
        "Saved" to "সেভ হয়েছে",
        "Print / PDF" to "প্রিন্ট / PDF",
        "Share Report" to "রিপোর্ট শেয়ার করুন",
        "Add Sheet" to "নতুন শিট যোগ করুন",
        "Delete Sheet" to "শিট মুছুন",
        "Confirm Delete" to "মুছে ফেলতে নিশ্চিত করুন?",
        "Are you sure you want to delete this sheet?" to "আপনি কি নিশ্চিতভাবে এই শিটটি মুছে ফেলতে চান?",
        "Are you sure you want to reset all data?" to "আপনি কি নিশ্চিতভাবে সমস্ত ডেটা মুছে ফেলতে চান?",
        "Delete" to "মুছুন",
        "Close" to "বন্ধ করুন"
    )

    private val STRINGS_AR = mapOf(
        "Time Sheet" to "جدول الدوام",
        "Duty & overtime tracker" to "متابعة الدوام والعمل الإضافي",
        "Monthly Hours" to "ساعات الشهر",
        "Sheet Hours" to "ساعات الورقة",
        "Monthly Earnings" to "دخل الشهر",
        "Sheet Earnings" to "دخل الورقة",
        "Hourly Rate" to "أجر الساعة",
        "Daily Breakdown" to "التفاصيل اليومية",
        "Status" to "الحالة",
        "Date" to "التاريخ",
        "Basic" to "أساسي",
        "OT" to "إضافي",
        "Total" to "المجموع",
        "Earnings" to "الدخل",
        "Not entered" to "غير مُدخل",
        "Present" to "حاضر",
        "Absent" to "غائب",
        "Holiday / Off" to "عطلة / إجازة",
        "Basic Hours" to "الساعات الأساسية",
        "Overtime Hours" to "ساعات إضافية",
        "Total Hours" to "إجمالي الساعات",
        "Working Days" to "أيام العمل",
        "Lifetime Earnings" to "إجمالي الدخل الكلي",
        "Lifetime Hours" to "إجمالي الساعات الكلي",
        "Advances" to "السلف",
        "+ Add Advance" to "+ إضافة سلفة",
        "Net Payable (Earnings − Advances)" to "الصافي المستحق (الدخل − السلف)",
        "Settings" to "الإعدادات",
        "Appearance" to "المظهر",
        "Dark mode" to "الوضع الداكن",
        "Light mode" to "الوضع الفاتح",
        "Language" to "اللغة",
        "Overtime" to "العمل الإضافي",
        "Normal day OT rate" to "معدل الإضافي في الأيام العادية",
        "Timesheet" to "الجدول",
        "Quick Fill Month" to "تعبئة الشهر السريعة",
        "Data" to "البيانات",
        "Backup Data" to "نسخ احتياطي",
        "Restore Data" to "استعادة البيانات",
        "Reset Data" to "إعادة ضبط البيانات",
        "Basic hours" to "الساعات الأساسية",
        "OT hours" to "ساعات الإضافي",
        "Skip weekly off days" to "تخطي أيام الإجازة الأسبوعية",
        "Only fill empty days" to "تعبئة الأيام الفارغة فقط",
        "Up to today only" to "حتى اليوم فقط",
        "Cancel" to "إلغاء",
        "Your name" to "اسمك",
        "Trade" to "المهنة",
        "Company" to "الشركة",
        "Sub-contractor" to "مقاول من الباطن",
        "Amount" to "المبلغ",
        "Note (optional)" to "ملاحظة (اختياري)",
        "days" to "أيام",
        "Advance" to "السلف",
        "Weekly Off" to "الإجازة الأسبوعية",
        "None" to "لا يوجد",
        "Friday" to "الجمعة",
        "Saturday" to "السبت",
        "Sunday" to "الأحد",
        "Friday + Saturday" to "الجمعة + السبت",
        "Earnings in Report" to "الدخل في التقرير",
        "ON" to "تشغيل",
        "OFF" to "إيقاف",
        "Fill" to "تعبئة",
        "Sheet" to "ورقة",
        "Saved" to "تم الحفظ",
        "Print / PDF" to "طباعة / PDF",
        "Share Report" to "مشاركة التقرير",
        "Add Sheet" to "إضافة ورقة",
        "Delete Sheet" to "حذف الورقة",
        "Confirm Delete" to "تأكيد الحذف",
        "Are you sure you want to delete this sheet?" to "هل أنت متأكد أنك تريد حذف هذه الورقة؟",
        "Are you sure you want to reset all data?" to "هل أنت متأكد من إعادة تعيين كافة البيانات؟",
        "Delete" to "حذف",
        "Close" to "إغلاق"
    )

    fun t(key: String, lang: String): String {
        return when (lang) {
            "bn" -> STRINGS_BN[key] ?: key
            "ar" -> STRINGS_AR[key] ?: key
            else -> key
        }
    }

    fun getDayOfWeekName(dayOfWeek: Int, lang: String): String {
        val safeIndex = dayOfWeek.coerceIn(0, 6)
        return when (lang) {
            "bn" -> DAYS_OF_WEEK_BN[safeIndex]
            "ar" -> DAYS_OF_WEEK_AR[safeIndex]
            else -> DAYS_OF_WEEK_EN[safeIndex]
        }
    }

    fun formatMoney(amount: Double, currency: String): String {
        val sym = CURRENCY_SYMBOLS[currency] ?: currency
        val dec = CURRENCY_DECIMALS[currency] ?: 2
        return String.format(Locale.US, "$sym %.${dec}f", amount)
    }

    fun formatHours(hours: Double, includeUnit: Boolean = true): String {
        val str = String.format(Locale.US, "%.2f", hours)
        return if (includeUnit) "$str h" else str
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getDayOfWeek(year: Int, month: Int, day: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday, 1=Monday, ..., 6=Saturday
    }

    fun getCurrentMonthString(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", y, m)
    }

    fun getCurrentDateString(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }
}
