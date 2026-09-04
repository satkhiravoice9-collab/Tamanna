package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

// ================= গ্লোবাল থিম ম্যানেজার (পুরো অ্যাপের জন্য) =================
object ThemeManager {
    class ThemeColors(
        val bgMain: Int, val cardBg: Int, val cardStroke: Int,
        val textAccent: Int, val btnBg: Int, val textMain: Int, val textSub: Int
    )
    fun getTheme(context: Context): ThemeColors {
        val themeName = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE).getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")
        return when {
            themeName?.contains("সাদা") == true -> ThemeColors(
                Color.parseColor("#F3F4F6"), Color.WHITE, Color.parseColor("#E5E7EB"),
                Color.parseColor("#059669"), Color.parseColor("#FBBF24"), Color.BLACK, Color.DKGRAY
            )
            themeName?.contains("কাবা") == true -> ThemeColors(
                Color.parseColor("#111827"), Color.parseColor("#1F2937"), Color.parseColor("#374151"),
                Color.parseColor("#FBBF24"), Color.parseColor("#F59E0B"), Color.WHITE, Color.LTGRAY
            )
            themeName?.contains("সুবহে") == true -> ThemeColors(
                Color.parseColor("#451A03"), Color.parseColor("#78350F"), Color.parseColor("#92400E"),
                Color.parseColor("#FDE047"), Color.parseColor("#FACC15"), Color.WHITE, Color.parseColor("#FEF3C7")
            )
            else -> ThemeColors( // মদিনা থিম (ডিফল্ট)
                Color.parseColor("#091C14"), Color.parseColor("#114D3C"), Color.parseColor("#1B785B"),
                Color.parseColor("#FBBF24"), Color.parseColor("#FACC15"), Color.WHITE, Color.parseColor("#D1D5DB")
            )
        }
    }
}

class ProfileSettingsActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // ডাইনামিক থিম লোড
    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSettingsPage()
    }

    private fun showSettingsPage() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(themeColors.textMain); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "⚙️ প্রোফাইল ও সেটিংস"; textSize = 17f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        // ================= উদ্যোক্তা কার্ড =================
        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(themeColors.textMain); textSize = 15f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(4).toFloat(), 1f) })
        
        devCard.addView(Button(this).apply {
            text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(themeColors.btnBg)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }
            setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))) }
        })
        devCard.addView(Button(this).apply {
            text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))) }
        })
        content.addView(devCard)

        // ================= গুগল ক্লাউড সাইন ইন কার্ড =================
        val cloudCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        cloudCard.addView(TextView(this).apply { text = "☁️ গুগল ক্লাউড সাইন ইন ও সিঙ্ক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", "কোনো অ্যাকাউন্ট যুক্ত নেই")
        
        cloudCard.addView(TextView(this).apply { text = "সংযুক্ত ক্লাউড জিমেইল:\n$savedEmail\n(১০০% গুগল ক্লাউডে ডাটা সংরক্ষণ হবে)"; setTextColor(themeColors.textMain); textSize = 14f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(2).toFloat(), 1f) })
        
        cloudCard.addView(Button(this).apply {
            text = "🔵 গুগল দিয়ে সরাসরি সাইন ইন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#60A5FA"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { showGoogleSignInDialog() } // ডায়ালগ ওপেন হবে
        })
        content.addView(cloudCard)

        // ================= অ্যাপ থিম নির্বাচন =================
        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })

        val savedTheme = sharedPrefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")
        val themes = listOf(
            Pair("⚪ সাদা থিম (লাইট)", "#F3F4F6"), Pair("⬛ কাবা থিম (ডার্ক গোল্ড)", "#1F2937"), 
            Pair("🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "#064E3B"), Pair("🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)", "#B45309")
        )

        themes.forEach { (tName, tColor) ->
            val isSelected = savedTheme == tName
            themeCard.addView(Button(this).apply {
                text = if (isSelected) "✅ $tName" else tName
                isAllCaps = false; setTextColor(if(tName.contains("সাদা")) Color.BLACK else Color.WHITE)
                background = GradientDrawable().apply { setColor(Color.parseColor(tColor)); cornerRadius = dp(8).toFloat(); if (isSelected) setStroke(dp(3), themeColors.btnBg) else setStroke(dp(1), Color.GRAY) }
                layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(10) }
                
                setOnClickListener {
                    sharedPrefs.edit().putString("app_theme", tName).apply()
                    Toast.makeText(this@ProfileSettingsActivity, "থিম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                    // থিম পরিবর্তন হলে সাথে সাথে পেজ রিলোড হবে
                    startActivity(intent); finish(); overridePendingTransition(0, 0)
                }
            })
        }
        content.addView(themeCard)

        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    // ================= ভিডিওর মতো গুগল একাউন্ট ডায়ালগ =================
    private fun showGoogleSignInDialog() {
        val emails = listOf("sabbirnumber@gmail.com", "satkhiravoice9@gmail.com", "sabbirahmadblog@gmail.com", "muhammadsabbirahmad@gmail.com", "Add account")
        
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(20), dp(20), dp(20)); setBackgroundColor(Color.WHITE) }
        dialogView.addView(TextView(this).apply { text = "Choose an account"; textSize = 20f; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK); setPadding(0, 0, 0, dp(15)) })

        val radioGroup = RadioGroup(this)
        emails.forEachIndexed { i, email ->
            val rb = RadioButton(this).apply { text = email; textSize = 16f; setTextColor(Color.BLACK); setPadding(0, dp(10), 0, dp(10)) }
            radioGroup.addView(rb)
            if (i == 0) rb.isChecked = true
        }
        dialogView.addView(ScrollView(this).apply { addView(radioGroup) }, LinearLayout.LayoutParams(-1, dp(200), 1f))

        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; setPadding(0, dp(10), 0, 0) }
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnRow.addView(Button(this).apply { text = "Cancel"; setTextColor(Color.parseColor("#059669")); setBackgroundColor(Color.TRANSPARENT); setOnClickListener { dialog.dismiss() } })
        btnRow.addView(Button(this).apply { 
            text = "OK"; setTextColor(Color.parseColor("#059669")); setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                val selectedText = radioGroup.findViewById<RadioButton>(selectedId).text.toString()
                getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", selectedText).apply()
                Toast.makeText(this@ProfileSettingsActivity, "একাউন্ট যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                showSettingsPage() // UI আপডেট হবে
            }
        })
        dialogView.addView(btnRow)
        dialog.show()
    }
}
