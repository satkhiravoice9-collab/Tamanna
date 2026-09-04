package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray

class TasbihActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private val bgMain get() = themeColors.bgMain
    private val textAccent get() = themeColors.textAccent
    private val textMain get() = themeColors.textMain
    private val btnBg get() = themeColors.btnBg

    private var currentCount = 0
    private var isCustomMode = false
    private var customZikirId = ""
    private var customZikirName = ""
    private var customTarget = 0
    private var hasShownPopup = false

    private lateinit var countTextView: TextView

    private fun getBtnDrawable(color: Int, radius: Int = 6) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        customZikirId = intent.getStringExtra("ZIKIR_ID") ?: ""
        if (customZikirId.isNotEmpty()) {
            isCustomMode = true
            customZikirName = intent.getStringExtra("ZIKIR_NAME") ?: ""
            customTarget = intent.getIntExtra("ZIKIR_TARGET", 0)
            currentCount = intent.getIntExtra("ZIKIR_READ", 0)
        } else {
            currentCount = getSharedPreferences("TasbihData", Context.MODE_PRIVATE).getInt("main_count", 0)
        }

        buildUI()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain)
            setOnClickListener { incrementCount() }
        }

        // ================= ১. টপ বার =================
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        top.addView(TextView(this).apply { 
            text = if (isCustomMode) "🕋 $customZikirName" else "🕋 সাধারণ তাসবিহ কাউন্টার"
            textSize = 18f; setTextColor(textMain); setTypeface(null, Typeface.BOLD) 
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(0, -2, 1f))
        
        top.addView(Button(this).apply {
            text = "রিসেট (০)"; isAllCaps = false; textSize = 13f; setTextColor(textMain)
            background = getBtnDrawable(Color.parseColor("#475569"), 4)
            layoutParams = LinearLayout.LayoutParams(dp(85), dp(38))
            setOnClickListener { currentCount = 0; hasShownPopup = false; updateDisplay(); saveProgress() }
        })
        root.addView(top)

        // ================= ২. কাউন্টার ও কাবার ইমেজ সেকশন =================
        val centerLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(20), dp(10), dp(20), dp(10)) }

        val kaabaBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0F172A")) 
                setStroke(dp(2), Color.parseColor("#FBBF24")) 
                cornerRadius = dp(14).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(dp(190), dp(175)).apply { bottomMargin = dp(15) }
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        // আপলোড করা kaaba_img ছবি এখানে রেন্ডার করা হচ্ছে
        kaabaBox.addView(ImageView(this).apply {
            val imgResId = resources.getIdentifier("kaaba_img", "drawable", packageName)
            if (imgResId != 0) {
                setImageResource(imgResId)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(dp(130), dp(115)).apply { bottomMargin = dp(4) }
        })

        kaabaBox.addView(TextView(this).apply {
            text = "لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ"; textSize = 13f; setTextColor(Color.parseColor("#FBBF24"))
            setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
        })
        centerLayout.addView(kaabaBox)

        val savedTheme = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).getString("app_theme", "")
        val isLightMode = savedTheme?.contains("সাদা") == true

        countTextView = TextView(this).apply {
            textSize = 85f
            setTextColor(if (isLightMode) Color.BLACK else Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }
        centerLayout.addView(countTextView)

        if (isCustomMode) {
            centerLayout.addView(TextView(this).apply { text = "টার্গেট: ${bn(customTarget)} বার"; textSize = 16f; setTextColor(if(isLightMode) Color.parseColor("#047857") else Color.parseColor("#FBBF24")); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        } else {
            centerLayout.addView(TextView(this).apply { text = "মুক্ত গণনা (প্রতি ১০০ পূর্ণে ভাইব্রেশন)"; textSize = 15f; setTextColor(if(isLightMode) Color.parseColor("#047857") else Color.parseColor("#FBBF24")); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        }
        centerLayout.addView(TextView(this).apply { text = "👇 স্ক্রিনের যেকোনো জায়গায় ট্যাপ করে গণনা করুন"; textSize = 13f; setTextColor(textMain) })

        root.addView(centerLayout, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= ৩. অ্যাকশন বাটনসমূহ =================
        val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        if (isCustomMode) {
            actionRow.addView(Button(this).apply {
                text = "সাধারণ তাসবিহে ফিরে যান"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 12f; background = getBtnDrawable(Color.parseColor("#374151"))
                layoutParams = LinearLayout.LayoutParams(0, dp(45), 1f).apply { rightMargin = dp(5) }
                setOnClickListener { startActivity(Intent(this@TasbihActivity, TasbihActivity::class.java)); finish() }
            })
            actionRow.addView(Button(this).apply {
                text = "📋 জিকির তালিকা ও টার্গেট"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 12f; background = getBtnDrawable(Color.parseColor("#274E3E"))
                layoutParams = LinearLayout.LayoutParams(0, dp(45), 1f).apply { leftMargin = dp(5) }
                setOnClickListener { startActivity(Intent(this@TasbihActivity, ZikirManagerActivity::class.java)); finish() }
            })
        } else {
            actionRow.addView(Button(this).apply {
                text = "📋 জিকির তালিকা ও টার্গেট"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 14f; background = getBtnDrawable(Color.parseColor("#274E3E"), 8)
                layoutParams = LinearLayout.LayoutParams(-1, dp(45))
                setOnClickListener { startActivity(Intent(this@TasbihActivity, ZikirManagerActivity::class.java)) }
            })
        }
        root.addView(actionRow)

        // ================= ৪. বটম মেনু =================
        val menu = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(Color.parseColor("#0F172A")); setPadding(dp(2), dp(4), dp(2), dp(4)); elevation = dp(8).toFloat() }
        listOf("🏠\nহোম", "🕋\nতাসবিহ", "📚\nলাইব্রেরী", "📁\nআমল", "📝\nনোট", "🔄\nসিঙ্ক", "👤\nপ্রোফাইল").forEach { label ->
            menu.addView(Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER; setTextColor(Color.parseColor("#9CA3AF")); background = Color.TRANSPARENT.let { GradientDrawable() }
                if(label.contains("তাসবিহ")) setTextColor(Color.parseColor("#FBBF24"))
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@TasbihActivity, MainActivity::class.java)); finishAffinity() }
                        label.contains("লাইব্রেরী") -> startActivity(Intent(this@TasbihActivity, LibraryActivity::class.java))
                        label.contains("আমল") -> startActivity(Intent(this@TasbihActivity, MasnunAmolActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@TasbihActivity, NotepadActivity::class.java))
                        label.contains("প্রোফাইল") -> startActivity(Intent(this@TasbihActivity, ProfileSettingsActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
        updateDisplay()
    }

    private fun incrementCount() {
        currentCount++
        updateDisplay()
        saveProgress()

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (isCustomMode) {
            if (currentCount == customTarget && !hasShownPopup) {
                vibratePhone(v, 500)
                showTargetPopup()
                hasShownPopup = true
            } else { vibratePhone(v, 50) }
        } else {
            if (currentCount > 0 && currentCount % 100 == 0) { vibratePhone(v, 500) } 
            else { vibratePhone(v, 50) }
        }
    }

    private fun vibratePhone(vibrator: Vibrator, duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else { @Suppress("DEPRECATION") vibrator.vibrate(duration) }
        } catch (e: Exception) {}
    }

    private fun updateDisplay() { countTextView.text = bn(currentCount) }

    private fun saveProgress() {
        if (isCustomMode) {
            val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
            val jsonArray = JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == customZikirId) { obj.put("read", currentCount); break }
            }
            prefs.edit().putString("zikir_list", jsonArray.toString()).apply()
        } else {
            getSharedPreferences("TasbihData", Context.MODE_PRIVATE).edit().putInt("main_count", currentCount).apply()
        }
    }

    private fun showTargetPopup() {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain); setPadding(dp(20), dp(20), dp(20), dp(20)) }
        dialogLayout.addView(TextView(this).apply { text = "মাশাআল্লাহ!"; textSize = 22f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(10)) })
        dialogLayout.addView(TextView(this).apply { text = "আপনার নির্ধারিত টার্গেট (${bn(customTarget)} বার) পূর্ণ হয়েছে।"; textSize = 16f; setTextColor(textMain); gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(20)) })
        
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).setCancelable(false).create()
        dialogLayout.addView(Button(this).apply {
            text = "ঠিক আছে"; setTextColor(Color.BLACK); background = getBtnDrawable(btnBg); layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener { dialog.dismiss() }
        })
        dialog.show()
    }
}
