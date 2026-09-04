package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfReaderActivity : ComponentActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private var bookName: String = ""
    private var targetPage: Int = 0

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookName = intent.getStringExtra("BOOK_NAME") ?: "Book"
        val downloadUrl = intent.getStringExtra("DOWNLOAD_URL") ?: ""
        targetPage = intent.getIntExtra("TARGET_PAGE", 0)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#091C14"))
        }

        // --- টপ বার (Top Bar) ---
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setBackgroundColor(Color.parseColor("#091C14"))
        }

        topBar.addView(TextView(this).apply {
            text = "← বের হন"
            textSize = 15f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); setPadding(0, 0, dp(12), 0)
            setOnClickListener { finish() }
        })

        topBar.addView(TextView(this).apply {
            text = bookName
            textSize = 15f; setTextColor(Color.WHITE); isSingleLine = true; ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
        }, LinearLayout.LayoutParams(0, -2, 1f))

        topBar.addView(Button(this).apply {
            text = "📑 বুকমার্কস"
            textSize = 12f; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#FBBF24"))
            setPadding(dp(8), 0, dp(8), 0); layoutParams = LinearLayout.LayoutParams(-2, dp(35))
            setOnClickListener { showBookmarksDialog() }
        })
        root.addView(topBar)

        // --- পিডিএফ ভিউ (মাঝখানে) ---
        val pdfContainer = RelativeLayout(this).apply { setBackgroundColor(Color.WHITE) }
        pdfView = PDFView(this, null)
        pdfContainer.addView(pdfView, RelativeLayout.LayoutParams(-1, -1))

        val loadingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(Color.parseColor("#E6FFFFFF"))
        }
        progressBar = ProgressBar(this)
        statusText = TextView(this).apply { text = "ফাইল চেক করা হচ্ছে..."; textSize = 16f; setTextColor(Color.BLACK); setPadding(0, dp(20), 0, 0) }
        loadingLayout.addView(progressBar); loadingLayout.addView(statusText)
        pdfContainer.addView(loadingLayout, RelativeLayout.LayoutParams(-1, -1))
        root.addView(pdfContainer, LinearLayout.LayoutParams(-1, 0, 1f))

        // --- বটম বার (Bottom Bar - পেজ সার্চ ও নতুন বুকমার্ক) ---
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#091C14"))
        }

        val pageInput = EditText(this).apply {
            hint = "পৃষ্ঠা নং"
            setHintTextColor(Color.GRAY); setTextColor(Color.BLACK); textSize = 14f; inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(dp(70), -2).apply { setMargins(0, 0, dp(8), 0) }
        }
        bottomBar.addView(pageInput)

        bottomBar.addView(Button(this).apply {
            text = "যান ➔"
            textSize = 13f; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#10B981"))
            setPadding(dp(8), 0, dp(8), 0); layoutParams = LinearLayout.LayoutParams(-2, dp(38))
            setOnClickListener {
                val p = pageInput.text.toString().toIntOrNull()
                if (p != null && p > 0) pdfView.jumpTo(p - 1)
                pageInput.text.clear()
            }
        })

        bottomBar.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f)) // Spacer

        bottomBar.addView(Button(this).apply {
            text = "➕ বুকমার্ক করুন"
            textSize = 13f; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#FACC15"))
            setPadding(dp(8), 0, dp(8), 0); layoutParams = LinearLayout.LayoutParams(-2, dp(38))
            setOnClickListener { addBookmarkDialog() }
        })
        root.addView(bottomBar)

        setContentView(root)

        val safeFileName = bookName.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
        val file = File(filesDir, safeFileName)

        if (file.exists()) {
            loadingLayout.visibility = View.GONE; openPdf(file)
        } else {
            downloadAndOpenPdf(downloadUrl, file, loadingLayout)
        }
    }

    private fun downloadAndOpenPdf(urlStr: String, file: File, loadingLayout: LinearLayout) {
        statusText.text = "⏳ ডাউনলোড হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন\n(বড় ফাইলের জন্য সময় লাগতে পারে)"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlStr); val connection = url.openConnection(); connection.connect()
                val input = connection.getInputStream(); val output = FileOutputStream(file)
                val data = ByteArray(4096); var count: Int
                while (input.read(data).also { count = it } != -1) output.write(data, 0, count)
                output.flush(); output.close(); input.close()
                withContext(Dispatchers.Main) { loadingLayout.visibility = View.GONE; openPdf(file) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "❌ ডাউনলোডে সমস্যা হয়েছে। ইন্টারনেট চেক করে আবার চেষ্টা করুন।"
                    progressBar.visibility = View.GONE
                    if (file.exists()) file.delete()
                }
            }
        }
    }

    private fun openPdf(file: File) {
        val sharedPref = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE)
        val startPage = if (targetPage > 0) targetPage - 1 else sharedPref.getInt(bookName, 0)

        pdfView.fromFile(file)
            .defaultPage(startPage)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .onPageChange { page, _ -> sharedPref.edit().putInt(bookName, page).apply() }
            .enableDoubletap(true)
            .load()
    }

    // ================= বুকমার্ক লজিক (JSON) =================
    private fun getBookmarks(): JSONArray {
        val prefs = getSharedPreferences("PdfBookmarks", Context.MODE_PRIVATE)
        return JSONArray(prefs.getString(bookName, "[]") ?: "[]")
    }

    private fun saveBookmarks(array: JSONArray) {
        getSharedPreferences("PdfBookmarks", Context.MODE_PRIVATE).edit().putString(bookName, array.toString()).apply()
    }

    private fun addBookmarkDialog() {
        val currentPage = pdfView.currentPage
        val edit = EditText(this).apply { setText("পৃষ্ঠা ${bn(currentPage + 1)}") }

        AlertDialog.Builder(this).setTitle("নতুন বুকমার্ক").setView(edit)
            .setPositiveButton("সেভ করুন") { _, _ ->
                val title = edit.text.toString().trim()
                if (title.isNotEmpty()) {
                    val array = getBookmarks()
                    val obj = JSONObject().apply {
                        put("id", System.currentTimeMillis().toString())
                        put("title", title)
                        put("page", currentPage)
                    }
                    array.put(obj)
                    saveBookmarks(array)
                    Toast.makeText(this, "বুকমার্ক সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("বাতিল", null).show()
    }

    private fun showBookmarksDialog() {
        val array = getBookmarks()
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val dialog = AlertDialog.Builder(this).setTitle("আপনার বুকমার্কসমূহ").setView(dialogLayout).setNegativeButton("বন্ধ করুন", null).create()

        if (array.length() == 0) {
            dialogLayout.addView(TextView(this).apply { text = "কোনো বুকমার্ক নেই।"; textSize = 16f; setPadding(0, dp(10), 0, 0) })
        } else {
            val scroll = ScrollView(this)
            val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
                
                // বুকমার্ক টাইটেল (ক্লিক করলে পেজে চলে যাবে)
                row.addView(TextView(this).apply {
                    text = "🔖 ${obj.getString("title")}"
                    textSize = 16f; setTextColor(Color.parseColor("#114D3C")); setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                    setOnClickListener { pdfView.jumpTo(obj.getInt("page")); dialog.dismiss() }
                })
                
                // এডিট বাটন
                row.addView(Button(this).apply {
                    text = "✏️"
                    layoutParams = LinearLayout.LayoutParams(dp(45), dp(40)).apply { rightMargin = dp(4) }
                    setOnClickListener { dialog.dismiss(); editBookmarkDialog(obj, i) }
                })
                
                // ডিলিট বাটন
                row.addView(Button(this).apply {
                    text = "🗑️"
                    layoutParams = LinearLayout.LayoutParams(dp(45), dp(40))
                    setOnClickListener {
                        array.remove(i); saveBookmarks(array)
                        dialog.dismiss(); showBookmarksDialog() // রিফ্রেশ
                    }
                })
                list.addView(row)
                list.addView(View(this).apply { setBackgroundColor(Color.LTGRAY); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) })
            }
            scroll.addView(list)
            dialogLayout.addView(scroll)
        }
        dialog.show()
    }

    private fun editBookmarkDialog(obj: JSONObject, index: Int) {
        val edit = EditText(this).apply { setText(obj.getString("title")) }
        AlertDialog.Builder(this).setTitle("বুকমার্ক এডিট করুন").setView(edit)
            .setPositiveButton("আপডেট") { _, _ ->
                val newTitle = edit.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val array = getBookmarks()
                    array.getJSONObject(index).put("title", newTitle)
                    saveBookmarks(array)
                    showBookmarksDialog() // রিফ্রেশ
                }
            }.setNegativeButton("বাতিল") { _, _ -> showBookmarksDialog() }.show()
    }
}
