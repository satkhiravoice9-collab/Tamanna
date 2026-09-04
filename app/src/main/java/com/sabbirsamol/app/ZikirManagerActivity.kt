package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ZikirManagerActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    // থিম ম্যানেজারের সাথে কানেকশন
    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textAccent get() = themeColors.textAccent
    private val btnBg get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain
    private val textSub get() = themeColors.textSub

    private fun getCardDrawable() = GradientDrawable().apply { setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat() }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(6).toFloat() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showZikirList()
    }

    override fun onResume() {
        super.onResume()
        showZikirList() 
    }

    private fun getZikirList(): JSONArray {
        val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
        return JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")
    }

    private fun saveZikirList(array: JSONArray) {
        getSharedPreferences("ZikirManager", Context.MODE_PRIVATE).edit().putString("zikir_list", array.toString()).apply()
    }

    private fun showZikirList() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "←"; textSize = 22f; setTextColor(textMain); setPadding(0, 0, dp(12), 0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📄 জিকির তালিকা ও টার্গেট শিডিউল"; textSize = 18f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        content.addView(Button(this).apply {
            text = "➕ নতুন জিকির ও টার্গেট যুক্ত করুন"; isAllCaps = false; setTextColor(Color.BLACK); textSize = 15f; background = getBtnDrawable(btnBg)
            layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(16) }
            setOnClickListener { showAddZikirDialog() }
        })

        val listArray = getZikirList()
        if (listArray.length() == 0) {
            content.addView(TextView(this).apply { text = "কোনো জিকির যুক্ত করা হয়নি।"; setTextColor(textSub); gravity = Gravity.CENTER; setPadding(0, dp(40), 0, 0) })
        } else {
            for (i in 0 until listArray.length()) {
                val obj = listArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val target = obj.getInt("target")
                val read = obj.getInt("read")
                val isScheduled = obj.optBoolean("isScheduled", true)

                val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
                
                card.addView(TextView(this).apply { text = "⭕ $name"; setTextColor(textAccent); textSize = 18f; setTypeface(null, Typeface.BOLD) })
                card.addView(TextView(this).apply { text = "টার্গেট: ${bn(target)} বার (পড়া হয়েছে: ${bn(read)} বার)"; setTextColor(textMain); textSize = 14f; setPadding(0, dp(8), 0, dp(12)) })
                
                val scheduleRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; background = GradientDrawable().apply { setColor(bgMain); cornerRadius = dp(6).toFloat() }; setPadding(dp(10), dp(8), dp(10), dp(8)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
                scheduleRow.addView(TextView(this).apply { text = "⏰ সিডিউল: ০৯:০০ AM"; setTextColor(Color.parseColor("#60A5FA")); textSize = 13f }, LinearLayout.LayoutParams(0, -2, 1f))
                scheduleRow.addView(TextView(this).apply { text = if(isScheduled) "ON" else "OFF"; setTextColor(if(isScheduled) Color.parseColor("#10B981") else textSub); textSize = 12f; setTypeface(null, Typeface.BOLD) })
                card.addView(scheduleRow)

                val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END }
                btnRow.addView(TextView(this).apply { text = "Delete"; setTextColor(Color.parseColor("#EF4444")); textSize = 14f; setPadding(dp(10), dp(10), dp(20), dp(10)); setOnClickListener { listArray.remove(i); saveZikirList(listArray); showZikirList() } })
                btnRow.addView(Button(this).apply {
                    text = "পড়া শুরু করুন ➔"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 13f; background = getBtnDrawable(Color.parseColor("#2563EB"))
                    layoutParams = LinearLayout.LayoutParams(dp(130), dp(35))
                    setOnClickListener {
                        val intent = Intent(this@ZikirManagerActivity, TasbihActivity::class.java)
                        intent.putExtra("ZIKIR_ID", id); intent.putExtra("ZIKIR_NAME", name); intent.putExtra("ZIKIR_TARGET", target); intent.putExtra("ZIKIR_READ", read)
                        startActivity(intent)
                    }
                })
                card.addView(btnRow)
                content.addView(card)
            }
        }
        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    private fun showAddZikirDialog() {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain); setPadding(dp(20), dp(20), dp(20), dp(20)) }
        dialogLayout.addView(TextView(this).apply { text = "নতুন জিকির যুক্ত করুন"; textSize = 18f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(16)) })
        
        val nameInput = EditText(this).apply { hint = "জিকিরের নাম লিখুন"; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK); setBackgroundColor(Color.WHITE); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
        val targetInput = EditText(this).apply { hint = "টার্গেট সংখ্যা (যেমন: ৩৩)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK); setBackgroundColor(Color.WHITE); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) } }
        
        dialogLayout.addView(nameInput); dialogLayout.addView(targetInput)
        
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        
        btnRow.addView(Button(this).apply { text = "সেভ করুন"; setTextColor(Color.BLACK); background = getBtnDrawable(btnBg); layoutParams = LinearLayout.LayoutParams(0, dp(45), 1f).apply { rightMargin = dp(5) }; setOnClickListener {
            val name = nameInput.text.toString().trim()
            val targetStr = targetInput.text.toString().trim()
            if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                val list = getZikirList()
                val obj = JSONObject().apply { put("id", UUID.randomUUID().toString()); put("name", name); put("target", targetStr.toInt()); put("read", 0); put("isScheduled", true) }
                list.put(obj); saveZikirList(list); dialog.dismiss(); showZikirList()
            } else { Toast.makeText(this@ZikirManagerActivity, "সব তথ্য পূরণ করুন", Toast.LENGTH_SHORT).show() }
        } })
        
        btnRow.addView(Button(this).apply { text = "বাতিল"; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#475569")); layoutParams = LinearLayout.LayoutParams(0, dp(45), 1f).apply { leftMargin = dp(5) }; setOnClickListener { dialog.dismiss() } })
        
        dialogLayout.addView(btnRow)
        dialog.show()
    }
}
