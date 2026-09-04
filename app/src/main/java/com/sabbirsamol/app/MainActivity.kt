package com.sabbirsamol.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(s: String): String = s.map { if (it in '0'..'9') "০১২৩৪৫৬৭৮৯"[it - '0'] else it }.joinToString("")

    private lateinit var tvWaqtCountdownHeader: TextView
    private lateinit var tvRemainingCountdown: TextView
    private lateinit var tvSunriseTime: TextView
    private lateinit var tvSunsetTime: TextView
    
    private lateinit var tvHijriDate: TextView
    private lateinit var tvEnglishDate: TextView
    private lateinit var tvBengaliDate: TextView

    private lateinit var tvFajrTime: TextView
    private lateinit var tvZoharTime: TextView
    private lateinit var tvAsrTime: TextView
    private lateinit var tvMaghribTime: TextView
    private lateinit var tvIshaTime: TextView

    private lateinit var tvTahajjudTime: TextView
    private lateinit var tvSehriTime: TextView
    private lateinit var tvIftarTime: TextView

    private lateinit var tvHaramSunrise: TextView
    private lateinit var tvHaramZohar: TextView
    private lateinit var tvHaramSunset: TextView

    private lateinit var tvDohaTime: TextView
    private lateinit var tvJawalTime: TextView
    private lateinit var tvAwabinTime: TextView
    private lateinit var tvNafalTahajjud: TextView

    private val prayerRows = mutableMapOf<String, LinearLayout>()
    private val prayerNameViews = mutableMapOf<String, TextView>()
    private val prayerTimeViews = mutableMapOf<String, TextView>()

    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false
    private val timingsMap = mutableMapOf<String, String>()

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateLiveCountdown()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupProfessionalUI()
        loadCachedPrayerTimes()
        loadOnlinePrayerTimes()
    }

    override fun onResume() {
        super.onResume()
        startCountdownTimer()
    }

    override fun onPause() {
        super.onPause()
        stopCountdownTimer()
    }

    private fun startCountdownTimer() {
        if (!isTimerRunning) {
            handler.post(timerRunnable)
            isTimerRunning = true
        }
    }

    private fun stopCountdownTimer() {
        handler.removeCallbacks(timerRunnable)
        isTimerRunning = false
    }

    private fun setupProfessionalUI() {
        val theme = ThemeManager.getTheme(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(theme.bgMain)
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(75))
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(dp(4), dp(4), dp(4), dp(6))
        }

        topBar.addView(TextView(this).apply {
            text = "🔄 রিফ্রেশ"
            textSize = 13f
            setTextColor(Color.parseColor("#3B82F6"))
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { loadOnlinePrayerTimes() }
        })
        content.addView(topBar)

        val countdownCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(theme.cardBg)
                setStroke(dp(1), theme.cardStroke)
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        tvHijriDate = TextView(this).apply {
            text = "আরবি তারিখ লোড হচ্ছে..."
            textSize = 14f
            setTextColor(theme.textAccent)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(4))
        }
        countdownCard.addView(tvHijriDate)

        tvEnglishDate = TextView(this).apply {
            text = "ইংরেজি তারিখ"
            textSize = 12f
            setTextColor(theme.textMain)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(2))
        }
        countdownCard.addView(tvEnglishDate)

        tvBengaliDate = TextView(this).apply {
            text = "বাংলা তারিখ"
            textSize = 12f
            setTextColor(theme.textSub)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        }
        countdownCard.addView(tvBengaliDate)

        updateDynamicDates()

        val cardDivider = View(this).apply {
            background = GradientDrawable().apply { setColor(theme.cardStroke) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(4), 0, dp(8)) }
        }
        countdownCard.addView(cardDivider)

        tvWaqtCountdownHeader = TextView(this).apply {
            text = "ওয়াক্ত শেষ হতে বাকি"
            textSize = 16f
            setTextColor(theme.textAccent)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(2))
        }
        countdownCard.addView(tvWaqtCountdownHeader)

        tvRemainingCountdown = TextView(this).apply {
            text = "০০:০০:০০"
            textSize = 28f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        }
        countdownCard.addView(tvRemainingCountdown)

        val infoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        infoRow.addView(TextView(this).apply { text = "📍 সাতক্ষীরা"; textSize = 12f; setTextColor(theme.textSub); setPadding(0, 0, dp(10), 0) })
        
        tvSunriseTime = TextView(this).apply { text = "🌅 সূর্যোদয়: --:--"; textSize = 11f; setTextColor(theme.textSub); setPadding(dp(4), 0, dp(4), 0) }
        infoRow.addView(tvSunriseTime)

        tvSunsetTime = TextView(this).apply { text = "🌇 সূর্যাস্ত: --:--"; textSize = 11f; setTextColor(theme.textSub); setPadding(dp(10), 0, 0, 0) }
        infoRow.addView(tvSunsetTime)

        countdownCard.addView(infoRow)
        content.addView(countdownCard)

        fun createUniformRow(key: String, name: String, timeTextView: TextView, isSwitchNeeded: Boolean, textColor: Int, subColor: Int): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(6), dp(8), dp(6))
            }
            val nameView = TextView(this).apply {
                text = name
                textSize = 14f
                setTextColor(textColor)
                setTypeface(null, Typeface.BOLD)
            }
            val rightLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            timeTextView.textSize = 12f
            timeTextView.setTextColor(subColor)
            timeTextView.setPadding(0, 0, if(isSwitchNeeded) dp(10) else 0, 0)

            rightLayout.addView(timeTextView)
            if (isSwitchNeeded) {
                rightLayout.addView(Switch(this).apply { 
                    isChecked = false
                    setOnCheckedChangeListener { _, isChecked ->
                        val status = if (isChecked) "চালু" else "বন্ধ"
                        Toast.makeText(this@MainActivity, "$name অ্যালার্ম $status", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            row.addView(nameView, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(rightLayout, LinearLayout.LayoutParams(-2, -2))

            prayerRows[key] = row
            prayerNameViews[key] = nameView
            prayerTimeViews[key] = timeTextView
            return row
        }

        fun createDivider(): View {
            return View(this).apply {
                background = GradientDrawable().apply { setColor(Color.parseColor("#CA8A04")) }
                layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(dp(4), dp(2), dp(4), dp(2)) }
            }
        }

        // ================= ৪. বক্স ১: পাঁচ ওয়াক্তের সময়সূচি =================
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FACC15"))
                setStroke(dp(1), Color.parseColor("#EAB308"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        prayerCard.addView(TextView(this).apply {
            text = "🕋 পাঁচ ওয়াক্ত নামাজের সময়সূচি (হানাফি)"
            textSize = 15f
            setTextColor(Color.parseColor("#78350F"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        tvFajrTime = TextView(this)
        tvZoharTime = TextView(this)
        tvAsrTime = TextView(this)
        tvMaghribTime = TextView(this)
        tvIshaTime = TextView(this)

        prayerCard.addView(createUniformRow("Fajr", "ফজর", tvFajrTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createDivider())
        prayerCard.addView(createUniformRow("Dhuhr", "যোহর", tvZoharTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createDivider())
        prayerCard.addView(createUniformRow("Asr", "আসর", tvAsrTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createDivider())
        prayerCard.addView(createUniformRow("Maghrib", "মাগরিব", tvMaghribTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createDivider())
        prayerCard.addView(createUniformRow("Isha", "এশা", tvIshaTime, true, Color.BLACK, Color.parseColor("#334155")))
        content.addView(prayerCard)

        // ================= ৫. বক্স ২: নফল সালাতের সময় =================
        val nafalCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#ECFDF5"))
                setStroke(dp(1), Color.parseColor("#10B981"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        nafalCard.addView(TextView(this).apply {
            text = "✨ নফল সালাতের সময়"
            textSize = 15f
            setTextColor(Color.parseColor("#065F46"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvDohaTime = TextView(this)
        tvJawalTime = TextView(this)
        tvAwabinTime = TextView(this)
        tvNafalTahajjud = TextView(this)

        nafalCard.addView(createUniformRow("Doha", "☀️ দুহা (ইশরাক ও চাশত)", tvDohaTime, false, Color.BLACK, Color.parseColor("#047857")))
        nafalCard.addView(createDivider())
        nafalCard.addView(createUniformRow("Jawal", "🕌 জাওয়াল শুরু (দুপুর)", tvJawalTime, false, Color.BLACK, Color.parseColor("#047857")))
        nafalCard.addView(createDivider())
        nafalCard.addView(createUniformRow("Awabin", "⛅ আওয়াবিন", tvAwabinTime, false, Color.BLACK, Color.parseColor("#047857")))
        nafalCard.addView(createDivider())
        nafalCard.addView(createUniformRow("NafalTahajjud", "🌙 তাহাজ্জুদ", tvNafalTahajjud, false, Color.BLACK, Color.parseColor("#047857")))
        content.addView(nafalCard)

        // ================= ৬. বক্স ৩: তাহাজ্জুদ ও সেহরি-ইফতার =================
        val specialCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#CBD5E1"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        specialCard.addView(TextView(this).apply {
            text = "🌙 তাহাজ্জুদ, সেহরি ও ইফতার"
            textSize = 15f
            setTextColor(Color.parseColor("#0F172A"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvTahajjudTime = TextView(this)
        tvSehriTime = TextView(this)
        tvIftarTime = TextView(this)

        specialCard.addView(createUniformRow("Tahajjud", "তাহাজ্জুদ", tvTahajjudTime, false, Color.BLACK, Color.parseColor("#475569")))
        specialCard.addView(createDivider())
        specialCard.addView(createUniformRow("Sehri", "সেহরির শেষ সময়", tvSehriTime, false, Color.BLACK, Color.parseColor("#475569")))
        specialCard.addView(createDivider())
        specialCard.addView(createUniformRow("Iftar", "ইফতারের সময়", tvIftarTime, false, Color.BLACK, Color.parseColor("#475569")))
        content.addView(specialCard)

        // ================= ৭. বক্স ৪: হারাম ওয়াক্ত =================
        val haramCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EF4444"))
                setStroke(dp(1), Color.parseColor("#DC2626"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        haramCard.addView(TextView(this).apply {
            text = "⚠️ হারাম ওয়াক্ত (নামাজ নিষিদ্ধ সময়)"
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvHaramSunrise = TextView(this)
        tvHaramZohar = TextView(this)
        tvHaramSunset = TextView(this)

        haramCard.addView(createUniformRow("HaramSunrise", "সূর্যোদয় নিষিদ্ধ সময়", tvHaramSunrise, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        haramCard.addView(createDivider())
        haramCard.addView(createUniformRow("HaramZohar", "দ্বিপ্রহর নিষিদ্ধ সময়", tvHaramZohar, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        haramCard.addView(createDivider())
        haramCard.addView(createUniformRow("HaramSunset", "সূর্যাস্ত নিষিদ্ধ সময়", tvHaramSunset, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        content.addView(haramCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরী", "📝\nনোট", "⚙️\nসেটিংস")
        navItems.forEach { label ->
            bottomNav.addView(Button(this).apply {
                text = label; textSize = 11f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("হোম")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("তাসবিহ") -> startActivity(Intent(this@MainActivity, TasbihActivity::class.java))
                        label.contains("লাইব্রেরী") -> startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@MainActivity, NotepadActivity::class.java))
                        label.contains("সেটিংস") -> startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun updateDynamicDates() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        if (hour >= 18) {
            now.add(Calendar.DAY_OF_MONTH, 1)
        }

        val hijriDays = (now.timeInMillis / (1000 * 60 * 60 * 24) - 2).toInt()
        val hijriMonthIndex = 2
        val hijriDayNum = (hijriDays % 29) + 1
        val monthNames = arrayOf(
            "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", 
            "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান", 
            "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
        )
        val hijriStr = "${bn(hijriDayNum.toString().take(2))} ${monthNames[hijriMonthIndex]} ১৪৪৮ হি."

        val engFormat = SimpleDateFormat("dd MMMM, yyyy (EEEE)", Locale("bn", "BD"))
        val engStr = engFormat.format(Date())

        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val bDay = ((dayOfYear + 16) % 365) + 1
        val bMonthIdx = ((dayOfYear + 16) / 30) % 12
        val bMonths = arrayOf("বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন", "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র")
        val bengaliStr = "${bn(bDay.toString())} ${bMonths[bMonthIdx]}, ১৪৩৩ বঙ্গাব্দ"

        tvHijriDate.text = "🌙 $hijriStr"
        tvEnglishDate.text = "📅 $engStr"
        tvBengaliDate.text = "🌾 $bengaliStr"
    }

    private fun loadCachedPrayerTimes() {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE)
        val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha")
        keys.forEach { key ->
            prefs.getString(key, null)?.let { timingsMap[key] = it }
        }
        applyTimingsToUI()
    }

    private fun saveTimingsToCache(times: Map<String, String>) {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE).edit()
        times.forEach { (k, v) -> prefs.putString(k, v) }
        prefs.apply()
    }

    private fun convertTo12Hour(time24: String?): String {
        if (time24.isNullOrEmpty()) return "--:--"
        return try {
            val parts = time24.trim().split(":")
            var hour = parts[0].toInt()
            val minute = parts[1]
            val ampm = if (hour >= 12) "PM" else "AM"
            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12
            bn(String.format(Locale.ENGLISH, "%02d:%s %s", hour, minute, ampm))
        } catch (e: Exception) {
            time24
        }
    }

    private fun adjustTime(time24: String?, minutesToAdd: Int): String {
        if (time24.isNullOrEmpty()) return "--:--"
        try {
            val parts = time24.trim().split(":")
            val totalMin = parts[0].toInt() * 60 + parts[1].toInt() + minutesToAdd
            val newHour = (totalMin / 60) % 24
            val newMin = totalMin % 60
            return convertTo12Hour(String.format(Locale.ENGLISH, "%02d:%02d", newHour, newMin))
        } catch (e: Exception) {
            return convertTo12Hour(time24)
        }
    }

    private fun applyTimingsToUI() {
        val fajr = timingsMap["Fajr"] ?: "04:30"
        val sunrise = timingsMap["Sunrise"] ?: "05:46"
        val dhuhr = timingsMap["Dhuhr"] ?: "12:03"
        val sunset = timingsMap["Sunset"] ?: timingsMap["Maghrib"] ?: "18:20"
        val maghrib = timingsMap["Maghrib"] ?: "18:20"
        val isha = timingsMap["Isha"] ?: "19:37"

        tvFajrTime.text = "${convertTo12Hour(fajr)} - ${convertTo12Hour(sunrise)}"
        tvZoharTime.text = "${convertTo12Hour(dhuhr)} - ০৪:৩৩ PM"
        tvAsrTime.text = "০৪:৩৩ PM - ${convertTo12Hour(maghrib)}"
        tvMaghribTime.text = "${convertTo12Hour(maghrib)} - ${convertTo12Hour(isha)}"
        tvIshaTime.text = "${convertTo12Hour(isha)} - ${convertTo12Hour(fajr)}"

        tvSunriseTime.text = "🌅 সূর্যোদয়: ${convertTo12Hour(sunrise)}"
        tvSunsetTime.text = "🌇 সূর্যাস্ত: ${convertTo12Hour(sunset)}"

        tvTahajjudTime.text = "১২:০০ AM - ${convertTo12Hour(fajr)}"
        tvSehriTime.text = "${convertTo12Hour(fajr)} এর পূর্বে"
        tvIftarTime.text = "${convertTo12Hour(maghrib)}"

        val sunriseEnd = adjustTime(sunrise, 15)
        val zoharForbiddenStart = adjustTime(dhuhr, -5)
        val zoharForbiddenEnd = convertTo12Hour(dhuhr)
        val sunsetStart = adjustTime(sunset, -15)

        tvHaramSunrise.text = "${convertTo12Hour(sunrise)} - $sunriseEnd"
        tvHaramZohar.text = "$zoharForbiddenStart - $zoharForbiddenEnd"
        tvHaramSunset.text = "$sunsetStart - ${convertTo12Hour(sunset)}"

        val dohaEnd = adjustTime(dhuhr, -10)
        tvDohaTime.text = "${convertTo12Hour(sunrise)} - $dohaEnd"
        tvJawalTime.text = "${convertTo12Hour(dhuhr)}"
        tvAwabinTime.text = "মাগরিবের পর - ${convertTo12Hour(isha)}"
        tvNafalTahajjud.text = "এশার পর - ${convertTo12Hour(fajr)}"

        updateLiveCountdown()
    }

    private fun loadOnlinePrayerTimes() {
        CoroutineScope(Dispatchers.Main).launch {
            val context = this@MainActivity
            val onlineTimes = withContext(Dispatchers.IO) {
                if (OnlinePrayerFetcher.isNetworkAvailable(context)) {
                    OnlinePrayerFetcher.fetchSatkhiraTimings()
                } else {
                    null
                }
            }

            if (onlineTimes != null) {
                timingsMap.clear()
                timingsMap.putAll(onlineTimes)
                saveTimingsToCache(onlineTimes)
                applyTimingsToUI()
                Toast.makeText(context, "সাতক্ষীরার লাইভ সময় আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else if (timingsMap.isEmpty()) {
                val defaultTimes = mapOf(
                    "Fajr" to "04:30", "Sunrise" to "05:46", "Dhuhr" to "12:03",
                    "Asr" to "15:31", "Sunset" to "18:20", "Maghrib" to "18:20", "Isha" to "19:37"
                )
                timingsMap.putAll(defaultTimes)
                applyTimingsToUI()
            }
        }
    }

    private fun updateLiveCountdown() {
        if (timingsMap.isEmpty()) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        fun parseMinutes(timeStr: String?): Int {
            if (timeStr.isNullOrEmpty()) return -1
            val parts = timeStr.trim().split(":")
            if (parts.size < 2) return -1
            return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        }

        val fajr = parseMinutes(timingsMap["Fajr"])
        val sunrise = parseMinutes(timingsMap["Sunrise"])
        val dhuhr = parseMinutes(timingsMap["Dhuhr"])
        val asr = 16 * 60 + 33
        val maghrib = parseMinutes(timingsMap["Maghrib"])
        val isha = parseMinutes(timingsMap["Isha"])

        var currentWaqtName = "তাহাজ্জুদ"
        var waqtThumbnail = "🌙"
        var targetEndMinutes = fajr
        var activeKey = "Tahajjud"

        when {
            currentMinutes in fajr until sunrise -> {
                currentWaqtName = "ফজর"
                waqtThumbnail = "🌅"
                targetEndMinutes = sunrise
                activeKey = "Fajr"
            }
            currentMinutes in sunrise until dhuhr -> {
                currentWaqtName = "চাশত / নিষিদ্ধ সময়"
                waqtThumbnail = "☀️"
                targetEndMinutes = dhuhr
                activeKey = "Haram"
            }
            currentMinutes in dhuhr until asr -> {
                currentWaqtName = "যোহর"
                waqtThumbnail = "🌤️"
                targetEndMinutes = asr
                activeKey = "Dhuhr"
            }
            currentMinutes in asr until maghrib -> {
                currentWaqtName = "আসর"
                waqtThumbnail = "⛅"
                targetEndMinutes = maghrib
                activeKey = "Asr"
            }
            currentMinutes in maghrib until isha -> {
                currentWaqtName = "মাগরিব"
                waqtThumbnail = "🌇"
                targetEndMinutes = isha
                activeKey = "Maghrib"
            }
            currentMinutes >= isha || currentMinutes < fajr -> {
                currentWaqtName = "এশা"
                waqtThumbnail = "🌃"
                targetEndMinutes = if (currentMinutes >= isha) (24 * 60) + fajr else fajr
                activeKey = "Isha"
            }
        }

        tvWaqtCountdownHeader.text = "$waqtThumbnail $currentWaqtName শেষ হতে বাকি"

        val currentTotalSec = (currentMinutes * 60) + currentSeconds
        val targetTotalSec = targetEndMinutes * 60
        var diffSec = targetTotalSec - currentTotalSec
        if (diffSec < 0) diffSec += (24 * 60 * 60)

        val h = diffSec / 3600
        val m = (diffSec % 3600) / 60
        val s = diffSec % 60

        val countdownStr = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
        tvRemainingCountdown.text = bn(countdownStr)

        highlightActiveWaqt(activeKey)
    }

    private fun highlightActiveWaqt(activeKey: String) {
        prayerRows.forEach { (key, row) ->
            if (key == activeKey) {
                // রানিং ওয়াক্তের জন্য একদম হালকা ও স্পষ্ট প্রিমিয়াম হাইলাইট কালার, যাতে টেক্সট ক্লিয়ার দেখায়
                row.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FEF08A")) // হালকা উজ্জ্বল হলুদ হাইলাইট
                    cornerRadius = dp(8).toFloat()
                }
                prayerNameViews[key]?.setTextColor(Color.parseColor("#78350F")) // ডিপ ব্রাউন/কালো টেক্সট
                prayerTimeViews[key]?.setTextColor(Color.parseColor("#78350F"))
            } else {
                row.background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                }
                prayerNameViews[key]?.setTextColor(Color.BLACK)
                prayerTimeViews[key]?.setTextColor(Color.parseColor("#334155"))
            }
        }
    }
}
