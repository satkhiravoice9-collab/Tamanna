package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotepadActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private var isInsideNote = false

    // ================= থিম ম্যানেজারের সাথে কানেকশন =================
    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textYellow get() = themeColors.textAccent
    private val btnYellow get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain

    private val noteBgColors = arrayOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2", "#114D3C", "#1F2937")
    private val textColors = arrayOf(Color.RED, Color.parseColor("#10B981"), Color.parseColor("#3B82F6"), Color.parseColor("#F59E0B"), Color.parseColor("#8B5CF6"), Color.BLACK, Color.WHITE)

    private fun getCardDrawable(bgColor: Int = cardBg) = GradientDrawable().apply {
        setColor(bgColor); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }
    private fun getCircleColorDrawable(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL; setColor(color); setStroke(dp(1), Color.GRAY)
    }

    private fun toHtmlSafe(spanned: Spanned): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.toHtml(spanned, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
            else @Suppress("DEPRECATION") Html.toHtml(spanned)
        } catch (e: Exception) { spanned.toString() }
    }

    private fun fromHtmlSafe(html: String): Spanned {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            else @Suppress("DEPRECATION") Html.fromHtml(html)
        } catch (e: Exception) { Spannable.Factory.getInstance().newSpannable(html) }
    }

    private fun parseColorSafe(colorStr: String, defaultColor: Int): Int {
        return try { Color.parseColor(colorStr) } catch (e: Exception) { defaultColor }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNotesList()
    }
    
    override fun onBackPressed() {
        if (isInsideNote) showNotesList() else super.onBackPressed()
    }

    private fun getNotes(): JSONArray {
        return try {
            val prefs = getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE)
            JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
        } catch (e: Exception) { JSONArray() }
    }

    private fun saveNotes(array: JSONArray) {
        getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE).edit().putString("notes_list", array.toString()).apply()
    }

    private fun showNotesList() {
        isInsideNote = false

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(textMain); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📝 কালার নোটপ্যাড"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        val notes = getNotes()
        if (notes.length() == 0) {
            listLayout.addView(TextView(this).apply { text = "কোনো নোট নেই। নিচে '+' এ চাপ দিয়ে নতুন নোট তৈরি করুন।"; setTextColor(textMain); textSize = 15f; gravity = Gravity.CENTER; setPadding(0, dp(50), 0, 0) })
        } else {
            for (i in 0 until notes.length()) {
                try {
                    val obj = notes.getJSONObject(i)
                    val title = obj.optString("title", "শিরোনামহীন")
                    val date = obj.optString("date", "")
                    val bgColorStr = obj.optString("bgColor", "#114D3C")
                    val bgColor = parseColorSafe(bgColorStr, Color.parseColor("#114D3C"))

                    val isLight = bgColorStr in listOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2")
                    val titleColor = if (isLight) Color.BLACK else Color.WHITE

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                        background = getCardDrawable(bgColor); setPadding(dp(14), dp(14), dp(14), dp(14))
                        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                        setOnClickListener { showViewOrEditNoteDialog(i, obj) }
                    }
                    card.addView(LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                        addView(TextView(this@NotepadActivity).apply { text = title; setTextColor(titleColor); textSize = 16f; setTypeface(null, Typeface.BOLD) })
                        addView(TextView(this@NotepadActivity).apply { text = date; setTextColor(Color.GRAY); textSize = 12f; setPadding(0, dp(4), 0, 0) })
                    })
                    card.addView(TextView(this).apply { text = "🗑️"; textSize = 20f; setPadding(dp(10), 0, 0, 0); setOnClickListener { notes.remove(i); saveNotes(notes); showNotesList() } })
                    listLayout.addView(card)
                } catch (e: Exception) { continue }
            }
        }
        scroll.addView(listLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomBar = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(dp(10), dp(10), dp(10), dp(10)); background = getCardDrawable() }
        bottomBar.addView(Button(this).apply { text = "＋ নতুন নোট তৈরি করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(45)); setOnClickListener { showAddEditNoteDialog(-1, null) } })
        root.addView(bottomBar)

        setContentView(root)
    }

    private fun showAddEditNoteDialog(index: Int, existingObj: JSONObject?) {
        val dialogScrollContainer = ScrollView(this).apply { isFillViewport = true }
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(cardBg) }
        
        var currentBgColor = existingObj?.optString("bgColor", "#FFFFFF") ?: "#FFFFFF"

        val titleInput = EditText(this).apply {
            hint = "নোটের শিরোনাম লিখুন"; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE); setPadding(dp(10), dp(10), dp(10), dp(10)); setText(existingObj?.optString("title") ?: "")
        }
        
        val contentInput = EditText(this).apply {
            hint = "নোটের বিবরণ লিখুন..."; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK)
            setBackgroundColor(parseColorSafe(currentBgColor, Color.WHITE)); minLines = 8; gravity = Gravity.TOP; setPadding(dp(10), dp(10), dp(10), dp(10))
            if (existingObj != null) setText(fromHtmlSafe(existingObj.optString("content", "")))
        }

        val formatToolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
        val formatRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        
        formatRow.addView(Button(this).apply { text = "B"; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK); background = getBtnDrawable(Color.WHITE); layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { rightMargin = dp(4) }; setOnClickListener { val s = contentInput.selectionStart; val e = contentInput.selectionEnd; if (s != -1 && e != -1 && s < e) contentInput.text.setSpan(StyleSpan(Typeface.BOLD), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) else Toast.makeText(this@NotepadActivity, "প্রথমে লেখা সিলেক্ট করুন", Toast.LENGTH_SHORT).show() } })
        formatRow.addView(Button(this).apply { text = "U"; paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG; setTextColor(Color.BLACK); background = getBtnDrawable(Color.WHITE); layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { rightMargin = dp(8) }; setOnClickListener { val s = contentInput.selectionStart; val e = contentInput.selectionEnd; if (s != -1 && e != -1 && s < e) contentInput.text.setSpan(UnderlineSpan(), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) else Toast.makeText(this@NotepadActivity, "প্রথমে লেখা সিলেক্ট করুন", Toast.LENGTH_SHORT).show() } })
        textColors.forEach { color -> formatRow.addView(View(this).apply { background = getCircleColorDrawable(color); layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply { rightMargin = dp(6); gravity = Gravity.CENTER_VERTICAL }; setOnClickListener { val s = contentInput.selectionStart; val e = contentInput.selectionEnd; if (s != -1 && e != -1 && s < e) contentInput.text.setSpan(ForegroundColorSpan(color), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) } }) }

        val bgToolbar = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(10)) }
        bgToolbar.addView(TextView(this).apply { text = "নোটের ব্যাকগ্রাউন্ড থিম (সাদা, লাল, সবুজ ইত্যাদি):"; setTextColor(Color.LTGRAY); textSize = 11f; setPadding(0, 0, 0, dp(4)) })
        
        val bgColorsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        noteBgColors.forEach { hexColor -> bgColorsRow.addView(View(this).apply { background = getCircleColorDrawable(Color.parseColor(hexColor)); layoutParams = LinearLayout.LayoutParams(dp(35), dp(35)).apply { rightMargin = dp(8) }; setOnClickListener { currentBgColor = hexColor; contentInput.setBackgroundColor(Color.parseColor(hexColor)); val isDark = hexColor == "#114D3C" || hexColor == "#1F2937"; contentInput.setTextColor(if (isDark) Color.WHITE else Color.BLACK); contentInput.setHintTextColor(if (isDark) Color.LTGRAY else Color.GRAY) } }) }
        bgToolbar.addView(HorizontalScrollView(this).apply { addView(bgColorsRow); isHorizontalScrollBarEnabled = false })

        dialogView.addView(TextView(this).apply { text = if (index == -1) "নতুন নোট তৈরি" else "নোট সম্পাদনা"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })
        dialogView.addView(titleInput, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        dialogView.addView(contentInput, LinearLayout.LayoutParams(-1, dp(200)).apply { bottomMargin = dp(5) })
        dialogView.addView(LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(TextView(this@NotepadActivity).apply { text = "টেক্সট ফরম্যাটিং ও কালার টুলস (লেখা সিলেক্ট করে চাপুন):"; setTextColor(Color.LTGRAY); textSize = 11f; setPadding(0, dp(8), 0, dp(4)) }); addView(formatRow) })
        dialogView.addView(bgToolbar)

        val dialog = AlertDialog.Builder(this).setView(dialogScrollContainer.apply { addView(dialogView) }).create()

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(0, dp(10), 0, 0) }
        btnLayout.addView(Button(this).apply { text = "সংরক্ষণ"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(5) }; setOnClickListener { val t = titleInput.text.toString().trim(); val htmlContent = toHtmlSafe(contentInput.text).trim(); if (t.isNotEmpty() && contentInput.text.toString().trim().isNotEmpty()) { val notes = getNotes(); val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()); val obj = JSONObject().apply { put("title", t); put("content", htmlContent); put("date", sdf.format(Date())); put("bgColor", currentBgColor) }; if (index == -1) notes.put(obj) else notes.put(index, obj); saveNotes(notes); dialog.dismiss(); if (isInsideNote) showViewOrEditNoteDialog(index, obj) else showNotesList() } else Toast.makeText(this@NotepadActivity, "শিরোনাম ও বিবরণ লিখুন", Toast.LENGTH_SHORT).show() } })
        btnLayout.addView(Button(this).apply { text = "বাতিল"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#E5E7EB")); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(5) }; setOnClickListener { dialog.dismiss() } })
        dialogView.addView(btnLayout)
        dialog.show()
    }

    private fun showViewOrEditNoteDialog(index: Int, obj: JSONObject) {
        isInsideNote = true 
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(textMain); setPadding(0,0,dp(12),0); setOnClickListener { showNotesList() } })
        top.addView(TextView(this).apply { text = obj.optString("title", ""); textSize = 17f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD); isSingleLine = true }, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(TextView(this).apply { text = "✏️"; textSize = 18f; setPadding(dp(8), 0, dp(8), 0); setOnClickListener { showAddEditNoteDialog(index, obj) } })
        top.addView(TextView(this).apply { text = "🗑️"; textSize = 18f; setPadding(dp(8), 0, 0, 0); setOnClickListener { val notes = getNotes(); notes.remove(index); saveNotes(notes); showNotesList() } })
        root.addView(top)

        val contentScroll = ScrollView(this).apply { setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val contentBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(parseColorSafe(obj.optString("bgColor", "#114D3C"), Color.parseColor("#114D3C"))); setPadding(dp(16), dp(16), dp(16), dp(16)) }
        contentBox.addView(TextView(this).apply { text = "তারিখ: ${obj.optString("date", "")}"; setTextColor(Color.parseColor("#9CA3AF")); textSize = 12f; setPadding(0, 0, 0, dp(12)) })
        
        val isLight = obj.optString("bgColor", "#114D3C") in listOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2")
        contentBox.addView(TextView(this).apply { text = fromHtmlSafe(obj.optString("content", "")); setTextColor(if (isLight) Color.BLACK else Color.WHITE); textSize = 16f })
        
        contentScroll.addView(contentBox)
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }
}
