package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class PdfBookItem(val fileName: String, val title: String, val id: String)

class LibraryActivity : ComponentActivity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")
    private var isInsideVolume = false

    // ================= থিম ম্যানেজারের সাথে কানেকশন =================
    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textYellow get() = themeColors.textAccent
    private val btnYellow get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain
    private val textSub get() = themeColors.textSub
    private val btnDark get() = Color.parseColor("#0C291F")
    private val btnRed = Color.parseColor("#DC2626")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    private fun getSafeFileName(title: String) = title.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
    private fun isFileExists(title: String) = File(filesDir, getSafeFileName(title)).exists()
    private fun deleteFileSilent(title: String) {
        val file = File(filesDir, getSafeFileName(title))
        if (file.exists()) file.delete()
        Toast.makeText(this, "ফাইলটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    // ================= সম্পূর্ণ কিতাবের ডাটাবেস =================
    private val quran = PdfBookItem("quran_full.pdf", "পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)", "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")

    private val bukhariVolList = listOf(
        PdfBookItem("bukhari_1.pdf", "সহীহ বুখারী ১ম খণ্ড", "1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"),
        PdfBookItem("bukhari_2.pdf", "সহীহ বুখারী ২য় খণ্ড", "1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"),
        PdfBookItem("bukhari_3.pdf", "সহীহ বুখারী ৩য় খণ্ড", "14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR"),
        PdfBookItem("bukhari_4.pdf", "সহীহ বুখারী ৪র্থ খণ্ড", "1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd"),
        PdfBookItem("bukhari_5.pdf", "সহীহ বুখারী ৫ম খণ্ড", "1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX"),
        PdfBookItem("bukhari_6.pdf", "সহীহ বুখারী ৬ষ্ঠ খণ্ড", "1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb"),
        PdfBookItem("bukhari_7.pdf", "সহীহ বুখারী ৭ম খণ্ড", "1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9"),
        PdfBookItem("bukhari_8.pdf", "সহীহ বুখারী ৮ম খণ্ড", "1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA"),
        PdfBookItem("bukhari_9.pdf", "সহীহ বুখারী ৯ম খণ্ড", "1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I"),
        PdfBookItem("bukhari_10.pdf", "সহীহ বুখারী ১০ম খণ্ড", "1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8")
    )

    private val muslimVolList = listOf(
        PdfBookItem("muslim_1.pdf", "সহীহ মুসলিম ১ম খণ্ড", "1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"),
        PdfBookItem("muslim_2.pdf", "সহীহ মুসলিম ২য় খণ্ড", "1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG"),
        PdfBookItem("muslim_3.pdf", "সহীহ মুসলিম ৩য় খণ্ড", "11Ul0Laj9YGGV37KYnep73RCUELz0yvU8"),
        PdfBookItem("muslim_4.pdf", "সহীহ মুসলিম ৪র্থ খণ্ড", "16cTetFECwjEPtSHeajv_WTQ11LTATtJp"),
        PdfBookItem("muslim_5.pdf", "সহীহ মুসলিম ৫ম খণ্ড", "129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z"),
        PdfBookItem("muslim_6.pdf", "সহীহ মুসলিম ৬ষ্ঠ খণ্ড", "1o4mAG-Qx6KSumsohKgUK2k8S7TAy3PKp"),
        PdfBookItem("muslim_7.pdf", "সহীহ মুসলিম ৭ম খণ্ড", "121hx1VQv0HCZnztDFrhJrP4XPBuEzuQW"),
        PdfBookItem("muslim_8.pdf", "সহীহ মুসলিম ৮ম খণ্ড", "1mjTLc_svuuKcUlnQXRu0e3PEAZUzn7_u")
    )

    private val abuDaudVolList = listOf(
        PdfBookItem("abudaud_1.pdf", "সুনান আবু দাউদ ১ম খণ্ড", "15RGRxSsJeKSfITiCU20AOm477jrXf7tX"),
        PdfBookItem("abudaud_2.pdf", "সুনান আবু দাউদ ২য় খণ্ড", "1LLjLCwj-CXLv6RHaTSHpfKK4zPBj6mRn"),
        PdfBookItem("abudaud_3.pdf", "সুনান আবু দাউদ ৩য় খণ্ড", "1KD64uPxeVDliawvEKwOFNaNfH883rR6X"),
        PdfBookItem("abudaud_4.pdf", "সুনান আবু দাউদ ৪র্থ খণ্ড", "1iuOGcHAegMxqJ3VZ7HiLNXTbz8a-iooI")
    )

    private val tirmidhiVolList = listOf(
        PdfBookItem("tirmidhi_1.pdf", "জামে আত-তিরমিযী ১ম খণ্ড", "1tN-N6skALr_G83cXreKmo0mRIqW_siGx"),
        PdfBookItem("tirmidhi_2.pdf", "জামে আত-তিরমিযী ২য় খণ্ড", "1FCwMfqmzNHHrbfQr3ho502obdHAaSGr4"),
        PdfBookItem("tirmidhi_3.pdf", "জামে আত-তিরমিযী ৩য় খণ্ড", "1Z5FxEDR_dcDFVgWTcx5lxfijVUSAxjcN"),
        PdfBookItem("tirmidhi_4.pdf", "জামে আত-তিরমিযী ৪র্থ খণ্ড", "1yIiNsyBpYnJV5y8gnWAor23VwKgpD4N6"),
        PdfBookItem("tirmidhi_5.pdf", "জামে আত-তিরমিযী ৫ম খণ্ড", "18GvNtMs_VK9fb1_oCyHR4nWwz-_6W53h"),
        PdfBookItem("tirmidhi_6.pdf", "জামে আত-তিরমিযী ৬ষ্ঠ খণ্ড", "1D1dnryvH8iOY69ixBtGPQuF3_kmPRgu_")
    )

    private val nasaiVolList = listOf(
        PdfBookItem("nasai_1.pdf", "সুনান আন-নাসায়ী ১ম খণ্ড", "1VH9AkmVv3apCXYPBRu0mI7qmNsU66sZZ"),
        PdfBookItem("nasai_2.pdf", "সুনান আন-নাসায়ী ২য় খণ্ড", "1t_F71oekP3F1Dc2SDY2I-L5eyDgtPcIR"),
        PdfBookItem("nasai_3.pdf", "সুনান আন-নাসায়ী ৩য় খণ্ড", "1mjp-ZSvvBsLzkDRXj87oSrH88ifzLIKF"),
        PdfBookItem("nasai_4.pdf", "সুনান আন-নাসায়ী ৪র্থ খণ্ড", "19yY0S3jLumKpxff3n_Kf6qoX8A6Fmwqi")
    )

    private val ibnMajahVolList = listOf(
        PdfBookItem("ibnmajah_1.pdf", "সুনান ইবনে মাজাহ ১ম খণ্ড", "12nAeV_DjOOCK2WJmrFSSIPyf2fcAyWzd"),
        PdfBookItem("ibnmajah_2.pdf", "সুনান ইবনে মাজাহ ২য় খণ্ড", "1B9ZNJrMmTW1sPXtAX3n2VZj4j4xjG-U6"),
        PdfBookItem("ibnmajah_3.pdf", "সুনান ইবনে মাজাহ ৩য় খণ্ড", "1GVexhALQ3ISCd241Zj7PkgpZG-116Q59")
    )

    private val groups = listOf(
        "সহীহ বুখারী শরীফ" to bukhariVolList,
        "সহীহ মুসলিম শরীফ" to muslimVolList,
        "সুনান আবু দাউদ" to abuDaudVolList,
        "জামে আত-তিরমিযী" to tirmidhiVolList,
        "সুনান আন-নাসায়ী" to nasaiVolList,
        "সুনান ইবনে মাজাহ" to ibnMajahVolList
    )

    private val paraList = listOf(
        Pair("পারা ১: আলিফ লাম মীম (الم) (পৃষ্ঠা ৩)", 3), Pair("পারা ২: সায়াকুল (سَيَقُولُ) (পৃষ্ঠা ২৪)", 24),
        Pair("পারা ৩: তিলকার রুসুল (تِلْكَ الرُّسُلُ) (পৃষ্ঠা ۴۴)", 44), Pair("পারা ৪: লান তানালু (لَنْ تَنَالُوا) (পৃষ্ঠা ৬৪)", 64),
        Pair("পারা ৫: ওয়াল মুহসানাতু (وَالْمُحْصَنَاتُ) (পৃষ্ঠা ۸৪)", 84), Pair("পারা ৬: লা ইয়ুহিব্বুল্লাহ (لَا يُحِبُّ اللَّهُ) (পৃষ্ঠা ১০৪)", 104),
        Pair("পারা ৭: ওয়া ইযা সামিউ (وَإِذَا سَمِعُوا) (পৃষ্ঠা ১২৪)", 124), Pair("পারা ۸: ওয়া লাউ আন্নানা (وَلَوْ أَنَّنَا) (পৃষ্ঠা ১৪৪)", 144),
        Pair("পারা ৯: ক্বালাল মালাউ (قَالَ الْمَلَأُ) (পৃষ্ঠা ১৬৪)", 164), Pair("পারা ১০: ওয়া'লামু (وَاعْلَمُوا) (পৃষ্ঠা ১৮৪)", 184),
        Pair("পারা ১১: ইয়া'তাযিরুন (يَعْتَذِرُونَ) (পৃষ্ঠা ২০৪)", 204), Pair("পারা ১২: ওয়া মা মিন দা-ব্বাহ (وَمَا مِنْ دَابَّةٍ) (পৃষ্ঠা ২২৪)", 224),
        Pair("পারা ১৩: ওয়া মা উবাররিউ (وَمَا أُبَرِّئُ) (পৃষ্ঠা ২৪৪)", 244), Pair("পারা ১৪: রুবামা (رُبَمَا) (পৃষ্ঠা ২৬৪)", 264),
        Pair("পারা ১৫: সুবহানাল্লাযী (سُبْحَانَ الَّذِي) (পৃষ্ঠা ২৮৪)", 284), Pair("পারা ১৬: ক্বালা আলাম (قَالَ أَلَمْ) (পৃষ্ঠা ৩০৪)", 304),
        Pair("পারা ১৭: ইক্বতারা বা লিন্নাস (اقْتَرَبَ لِلنَّاسِ) (পৃষ্ঠা ৩২৪)", 324), Pair("পারা ১৮: ক্বাদ আফলাহা (قَدْ أَفْلَحَ) (পৃষ্ঠা ৩৪৪)", 344),
        Pair("পারা ۱۹: ওয়া ক্বাল্লাযীনা (وَقَالَ الَّذِينَ) (পৃষ্ঠা ৩৬৪)", 364), Pair("পারা ২০: আম্মান খালাক্বা (أَمَّنْ خَلَقَ) (পৃষ্ঠা ৩৮৪)", 384),
        Pair("পারা ২১: উতলু মা উহিয়া (اتْلُ مَا أُوحِيَ) (পৃষ্ঠা ৪০৪)", 404), Pair("পারা ২২: ওয়া মাই-য়াক্বনুত (وَمَنْ يَقْنُتْ) (পৃষ্ঠা ৪২৪)", 424),
        Pair("পারা ২৩: ওয়া মালি-য়া (وَمَا لِيَ) (পৃষ্ঠা ৪৪৪)", 444), Pair("পারা ২৪: ফামান আজলামু (فَمَنْ أَظْلَمُ) (পৃষ্ঠা ৪৬৪)", 464),
        Pair("পারা ২৫: ইলাইহি ইউরাদ্দু (إِلَيْهِ يُرَدُّ) (পৃষ্ঠা ৪৮৪)", 484), Pair("পারা ২৬: হা-মীম (حم) (পৃষ্ঠা ৫০৪)", 504),
        Pair("পারা ২৭: ক্বালা ফামা খাতবুকুম (قَالَ فَمَا خَطْبُكُمْ) (পৃষ্ঠা ৫২৪)", 524), Pair("পারা ২৮: ক্বাদ সামিআল্লাহ (قَدْ سَمِعَ اللَّهُ) (পৃষ্ঠা ৫৪৪)", 544),
        Pair("পারা ২৯: তাবারাকাল্লাযী (تَبَارَكَ الَّذِي) (পৃষ্ঠা ৫৬৪)", 564), Pair("পারা ৩০: আম্মা ইয়াতাসায়ালুন (عَمَّ يَتَسَاءَلُونَ) (পৃষ্ঠা ৫৮৪)", 584)
    )

    private val surahList = listOf(
        Pair("১. সূরা আল-ফাতিহা (পৃষ্ঠা ৩)", 3), Pair("২. সূরা আল-বাকারাহ (পৃষ্ঠা ৪)", 4),
        Pair("৩. সূরা আলে-ইমরান (পৃষ্ঠা ৫২)", 52), Pair("৪. সূরা আন-নিসা (পৃষ্ঠা ৭৯)", 79),
        Pair("৫. সূরা আল-মায়িদাহ (পৃষ্ঠা ১০৮)", 108), Pair("৬. সূরা আল-আনআম (পৃষ্ঠা ১৩০)", 130),
        Pair("৭. সূরা আল-আরাফ (পৃষ্ঠা ১৫৩)", 153), Pair("৮. সূরা আল-আনফাল (পৃষ্ঠা ১৭৯)", 179),
        Pair("৯. সূরা আত-তাওবাহ (পৃষ্ঠা ১৮৯)", 189), Pair("১০. সূরা ইউনুস (পৃষ্ঠা ২১০)", 210),
        Pair("১১. সূরা হুদ (পৃষ্ঠা ২২৩)", 223), Pair("১২. সূরা ইউসুফ (পৃষ্ঠা ২৩৭)", 237),
        Pair("১৩. সূরা আর-রাদ (পৃষ্ঠা ২৫১)", 251), Pair("১৪. সূরা ইব্রাহীম (পৃষ্ঠা ২৫৭)", 257),
        Pair("১৫. সূরা আল-হিজর (পৃষ্ঠা ২৬৩)", 263), Pair("১৬. সূরা আন-নাহল (পৃষ্ঠা ২৬۹)", 269),
        Pair("১৭. সূরা বনী ইসরাঈল (পৃষ্ঠা ২৮৪)", 284), Pair("১৮. সূরা আল-কাহফ (পৃষ্ঠা ২৯৫)", 295),
        Pair("১৯. সূরা মারইয়াম (পৃষ্ঠা ৩০৭)", 307), Pair("২০. সূরা ত্বা-হা (পৃষ্ঠা ৩১৪)", 314),
        Pair("২১. সূরা আল-আম্বিয়া (পৃষ্ঠা ৩২৪)", 324), Pair("২২. সূরা আল-হাজ্জ (পৃষ্ঠা ৩৩৫)", 335),
        Pair("২৩. সূরা আল-মুমিনুন (পৃষ্ঠা ৩৪৪)", 344), Pair("২৪. সূরা আন-নূর (পৃষ্ঠা ৩৫২)", 352),
        Pair("২৫. সূরা আল-ফুরকান (পৃষ্ঠা ৩৬১)", 361), Pair("২৬. সূরা আশ-শুয়ারা (পৃষ্ঠা ৩৬৮)", 368),
        Pair("২৭. সূরা আন-নামল (পৃষ্ঠা ৩৭৮)", 378), Pair("২৮. সূরা আল-কাসাস (পৃষ্ঠা ৩৮৭)", 387),
        Pair("২৯. সূরা আল-আনকাবুত (পৃষ্ঠা ৩৯৮)", 398), Pair("৩০. সূরা আর-রূম (পৃষ্ঠা ৪০৬)", 406),
        Pair("৩১. সূরা লুকমান (পৃষ্ঠা ৪১৩)", 413), Pair("৩২. সূরা আস-সাজদাহ (পৃষ্ঠা ৪১৭)", 417),
        Pair("৩৩. সূরা আল-আহযাব (পৃষ্ঠা ৪২০)", 420), Pair("৩৪. সূরা সাবা (পৃষ্ঠা ৪৩০)", 430),
        Pair("৩৫. সূরা ফাতির (পৃষ্ঠা ৪৩৬)", 436), Pair("৩৬. সূরা ইয়াসীন (পৃষ্ঠা ۴۴২)", 442),
        Pair("৩৭. সূরা আস-সাফফাত (পৃষ্ঠা ۴۴৭)", 447), Pair("৩৮. সূরা সোয়াদ (পৃষ্ঠা ۴۵৪)", 454),
        Pair("৩৯. সূরা আজ-জুমার (পৃষ্ঠা ৪৬০)", 460), Pair("৪০. সূরা আল-মু'মিন (গাফির) (পৃষ্ঠা ৪৬৯)", 469),
        Pair("৪১. সূরা হা-মীম সাজদাহ (পৃষ্ঠা ৪৭৯)", 479), Pair("৪২. সূরা আশ-শূরা (পৃষ্ঠা ৪৮৫)", 485),
        Pair("৪৩. সূরা আজ-জুখরুফ (পৃষ্ঠা ৪৯১)", 491), Pair("৪৪. সূরা আদ-দুখান (পৃষ্ঠা ৪৯৭)", 497),
        Pair("৪৫. সূরা আল-জাসিয়াহ (পৃষ্ঠা ৫০০)", 500), Pair("৪৬. সূরা আল-আহকাফ (পৃষ্ঠা ৫০۴)", 504),
        Pair("৪৭. সূরা মুহাম্মদ (পৃষ্ঠা ৫০৮)", 508), Pair("৪৮. সূরা আল-ফাতহ (পৃষ্ঠা ۵۱۳)", 513),
        Pair("৪৯. সূরা আল-হুজুরাত (পৃষ্ঠা ۵۱۷)", 517), Pair("৫০. সূরা কাফ (পৃষ্ঠা ৫২০)", 520),
        Pair("৫১. সূরা আজ-যারিয়াত (পৃষ্ঠা ৫২২)", 522), Pair("৫২. সূরা আত্ব-তূর (পৃষ্ঠা ৫২৫)", 525),
        Pair("৫৩. সূরা আন-নাজম (পৃষ্ঠা ৫২৮)", 528), Pair("৫৪. সূরা আল-কামার (পৃষ্ঠা ৫৩০)", 530),
        Pair("৫৫. সূরা আর-রহমান (পৃষ্ঠা ৫৩৩)", 533), Pair("৫৬. সূরা আল-ওয়াকিয়াহ (পৃষ্ঠা ৫৩৬)", 536),
        Pair("৫৭. সূরা আল-হাদীদ (পৃষ্ঠা ৫৩৯)", 539), Pair("৫৮. সূরা আল-মুজাদালাহ (পৃষ্ঠা ৫৪৪)", 544),
        Pair("৫৯. সূরা আল-হাশর (পৃষ্ঠা ৫৪৭)", 547), Pair("৬০. সূরা আল-মুমতাহিনাহ (পৃষ্ঠা ৫৫১)", 551),
        Pair("৬১. সূরা আস-সাফ (পৃষ্ঠা ৫৫৩)", 553), Pair("৬২. সূরা আল-জুমুআহ (পৃষ্ঠা ৫৫৫)", 555),
        Pair("৬৩. সূরা আল-মুনাফিকুন (পৃষ্ঠা ৫৫৬)", 556), Pair("৬৪. সূরা আত-তাগাবুন (পৃষ্ঠা ৫৫৮)", 558),
        Pair("৬৫. সূরা আত-ত্বালাক (পৃষ্ঠা ৫৬০)", 560), Pair("৬৬. সূরা আত-তাহরীম (পৃষ্ঠা ৫৬২)", 562),
        Pair("৬৭. সূরা আল-মুলক (পৃষ্ঠা ৫৬৬)", 566), Pair("৬৮. সূরা আল-কলম (পৃষ্ঠা ৫৬৬)", 566),
        Pair("৬৯. সূরা আল-হাক্কাহ (পৃষ্ঠা ৫৬৮)", 568), Pair("৭০. সূরা আল-মাআরিজ (পৃষ্ঠা ۵৭১)", 571),
        Pair("৭১. সূরা নূহ (পৃষ্ঠা ৫৭৩)", 573), Pair("৭২. সূরা আল-জ্বিন (পৃষ্ঠা ৫৭৫)", 575),
        Pair("৭৩. সূরা আল-মুযযাম্মিল (পৃষ্ঠা ৫৭৭)", 577), Pair("৭৪. সূরা আল-মুদ্দাসসির (পৃষ্ঠা ৫৭৯)", 579),
        Pair("৭৫. সূরা আল-কিয়ামাহ (পৃষ্ঠা ৫৮১)", 581), Pair("৭৬. সূরা আদ-দাহর (পৃষ্ঠা ৫৮৩)", 583),
        Pair("৭৭. সূরা আল-মুরসালাত (পৃষ্ঠা ৫৮৫)", 585), Pair("৭৮. সূরা আন-নাবা (পৃষ্ঠা ৫৮৭)", 587),
        Pair("৭۹. সূরা আন-নযিয়াত (পৃষ্ঠা ৫৮৮)", 588), Pair("৮০. সূরা আবাসা (পৃষ্ঠা ৫৯০)", 590),
        Pair("৮১. সূরা আত-তাকভীর (পৃষ্ঠা ৫৯১)", 591), Pair("৮২. সূরা আল-ইনফিতার (পৃষ্ঠা ৫৯২)", 592),
        Pair("৮৩. সূরা আল-মুতাফফিফীন (পৃষ্ঠা ৫৯২)", 592), Pair("৮৪. সূরা আল-ইনশিকাক (পৃষ্ঠা ৫৯৪)", 594),
        Pair("৮৫. সূরা আল-বুরূজ (পৃষ্ঠা ৫৯৫)", 595), Pair("৮৬. সূরা আত-তারিক (পৃষ্ঠা ৫৯৬)", 596),
        Pair("৮৭. সূরা আল-আ'লা (পৃষ্ঠা ৫৯৭)", 597), Pair("৮৮. সূরা আল-গাশিয়াহ (পৃষ্ঠা ৫৯৮)", 598),
        Pair("৮৯. সূরা আল-ফজর (পৃষ্ঠা ৫৯৯)", 599), Pair("৯০. সূরা আল-বালাদ (পৃষ্ঠা ৬০১)", 601),
        Pair("৯১. সূরা আশ-শামস (পৃষ্ঠা ৬০২)", 602), Pair("৯২. সূরা আল-লাইল (পৃষ্ঠা ৬০২)", 602),
        Pair("৯৩. সূরা আদ-দুহা (পৃষ্ঠা ৬০৩)", 603), Pair("৯৪. সূরা আল-ইনশিরাহ (পৃষ্ঠা ৬০৪)", 604),
        Pair("৯৫. সূরা আত-তীন (পৃষ্ঠা ৬০৪)", 604), Pair("৯৬. সূরা আল-আলাক (পৃষ্ঠা ৬০৫)", 605),
        Pair("৯৭. সূরা আল-কদর (পৃষ্ঠা ৬০৬)", 606), Pair("৯৮. সূরা আল-বাইয়্যিনাহ (পৃষ্ঠা ৬০৬)", 606),
        Pair("৯৯. সূরা আল-যিলযাল (পৃষ্ঠা ৬০৭)", 607), Pair("১০০. সূরা আল-আদিয়াত (পৃষ্ঠা ৬০৭)", 607),
        Pair("১০১. সূরা আল-কারিয়াহ (পৃষ্ঠা ৬০৮)", 608), Pair("১০২. সূরা আত-তাকাসুর (পৃষ্ঠা ৬০৯)", 609),
        Pair("১০৩. সূরা আল-আসর (পৃষ্ঠা ৬০৯)", 609), Pair("১০۴. সূরা আল-হুমাযাহ (পৃষ্ঠা ৬০৯)", 609),
        Pair("১০৫. সূরা আল-ফীল (পৃষ্ঠা ৬১০)", 610), Pair("১০৬. সূরা কুরাইশ (পৃষ্ঠা ৬১০)", 610),
        Pair("১০৭. সূরা আল-মাউন (পৃষ্ঠা ৬১০)", 610), Pair("১০৮. সূরা আল-কাউসার (পৃষ্ঠা ৬১১)", 611),
        Pair("১০৯. সূরা আল-কাফিরুন (পৃষ্ঠা ৬১১)", 611), Pair("১১০. সূরা আন-নাসর (পৃষ্ঠা ৬১১)", 611),
        Pair("১১۱. সূরা আল-লাহাব (পৃষ্ঠা ৬১১)", 611), Pair("১১২. সূরা আল-ইখলাস (পৃষ্ঠা ৬১২)", 612),
        Pair("১১৩. সূরা আল-ফালাক (পৃষ্ঠা ৬১২)", 612), Pair("১১৪. সূরা আন-নাস (পৃষ্ঠা ৬১২)", 612)
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showLibrary() }
    
    override fun onBackPressed() { if (isInsideVolume) showLibrary() else super.onBackPressed() }

    private fun showLibrary() {
        isInsideVolume = false 
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(textMain); setPadding(0, 0, dp(12), 0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📚 ইসলামিক লাইব্রেরী"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(80)) }

        val qCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
        qCard.addView(TextView(this).apply { text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ)"; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        if (isFileExists(quran.title)) {
            val qBtnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(0, dp(10), 0, dp(10)) }
            qBtnRow.addView(Button(this).apply { text = "📖 ৩০ পারা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }; setOnClickListener { showListDialog("৩০ পারা সূচিপত্র", paraList, quran) } })
            qBtnRow.addView(Button(this).apply { text = "📜 ১১৪ সুরা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#D97706")); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }; setOnClickListener { showListDialog("১১৪ সুরা সূচিপত্র", surahList, quran) } })
            qCard.addView(qBtnRow)
            
            val continueLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val savedPage = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE).getInt(quran.title, 0)
            continueLayout.addView(TextView(this).apply { text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage + 1)})"; setTextColor(textSub); textSize = 14f; layoutParams = LinearLayout.LayoutParams(0, -2, 1f); setOnClickListener { openPdf(quran, 0) } })
            continueLayout.addView(Button(this).apply { text = "🗑️"; setTextColor(Color.WHITE); background = getBtnDrawable(btnRed); layoutParams = LinearLayout.LayoutParams(dp(40), dp(35)); setOnClickListener { deleteFileSilent(quran.title); showLibrary() } })
            qCard.addView(continueLayout)
        } else {
            val dlBtn = Button(this).apply { text = "📥 কোরআন ডাউনলোড করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(10) } }
            dlBtn.setOnClickListener { startDownload(quran, dlBtn) { showLibrary() } }
            qCard.addView(dlBtn)
        }
        content.addView(qCard)

        content.addView(TextView(this).apply { text = "📂 সিহাহ সিত্তাহ হাদিস কিতাবসমূহ:"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, dp(6), 0, dp(12)) })

        groups.forEachIndexed { index, (title, books) ->
            val hCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
            hCard.addView(TextView(this).apply { text = "${bn(index + 1)}. $title (${bn(books.size)} খণ্ড)"; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD) })
            hCard.addView(Button(this).apply { text = "খুলুন ➔"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { topMargin = dp(10) }; setOnClickListener { showVolumes(title, books, index + 1) } })
            content.addView(hCard)
        }

        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    private fun showVolumes(title: String, books: List<PdfBookItem>, idxGroup: Int) {
        isInsideVolume = true 
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(textMain); setPadding(dp(10),0,dp(10),0); setOnClickListener { showLibrary() } })
        top.addView(TextView(this).apply { text = "📂 ${bn(idxGroup)}. $title"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        books.forEach { book ->
            val vCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
            vCard.addView(TextView(this).apply { text = book.title; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
            
            val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            if (isFileExists(book.title)) {
                val readBtn = Button(this).apply { text = "📖 পড়ুন"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(8) }; setOnClickListener { openPdf(book, 0) } }
                val delBtn = Button(this).apply { text = "🗑️"; setTextColor(Color.WHITE); background = getBtnDrawable(btnRed); layoutParams = LinearLayout.LayoutParams(dp(45), dp(40)); setOnClickListener { deleteFileSilent(book.title); showVolumes(title, books, idxGroup) } }
                actionRow.addView(readBtn); actionRow.addView(delBtn)
            } else {
                val dlBtn = Button(this).apply { text = "📥 ডাউনলোড করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(40)) }
                dlBtn.setOnClickListener { startDownload(book, dlBtn) { showVolumes(title, books, idxGroup) } }
                actionRow.addView(dlBtn)
            }
            vCard.addView(actionRow)
            list.addView(vCard)
        }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun startDownload(book: PdfBookItem, btn: Button, onComplete: () -> Unit) {
        btn.text = "⏳ ডাউনলোড হচ্ছে..."
        btn.isEnabled = false
        val file = File(filesDir, getSafeFileName(book.title))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://drive.google.com/uc?export=download&id=${book.id}")
                val connection = url.openConnection(); connection.connect()
                val input = connection.getInputStream(); val output = FileOutputStream(file)
                val data = ByteArray(4096); var count: Int
                while (input.read(data).also { count = it } != -1) output.write(data, 0, count)
                output.flush(); output.close(); input.close()
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { btn.text = "❌ ব্যর্থ হয়েছে!"; btn.isEnabled = true; if (file.exists()) file.delete() }
            }
        }
    }

    private fun showListDialog(titleText: String, items: List<Pair<String, Int>>, book: PdfBookItem) {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }
        dialogLayout.addView(TextView(this).apply { text = titleText; textSize = 18f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD); setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(themeColors.cardBg) })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        items.forEach { item ->
            list.addView(TextView(this).apply { text = item.first; textSize = 18f; setPadding(0, dp(12), 0, dp(12)); setTextColor(themeColors.textMain); setOnClickListener { openPdf(book, item.second); dialogLayout.parent?.let { (it as? AlertDialog)?.dismiss() } } })
            list.addView(View(this).apply { setBackgroundColor(themeColors.cardStroke); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) })
        }
        scroll.addView(list); dialogLayout.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()
        dialogLayout.addView(Button(this).apply { text = "বন্ধ করুন"; setTextColor(Color.BLACK); setBackgroundColor(themeColors.btnBg); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(16)) }; setOnClickListener { dialog.dismiss() } })
        dialog.show()
    }

    private fun openPdf(book: PdfBookItem, page: Int) {
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title); intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }
}
