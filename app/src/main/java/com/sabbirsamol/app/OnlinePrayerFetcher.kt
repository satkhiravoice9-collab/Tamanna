package com.sabbirsamol.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object OnlinePrayerFetcher {

    // ইন্টারনেট কানেকশন চেক করার ফাংশন
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // আল-আজান এপিআই থেকে সাতক্ষীরার লাইভ সময় আনা
    suspend fun fetchSatkhiraTimings(): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                // সাতক্ষীরা, বাংলাদেশ এর জন্য ফ্রি এপিআই এন্ডপয়েন্ট (Method 1: University of Islamic Sciences, Karachi)
                val urlString = "https://api.aladhan.com/v1/timingsByCity?city=Satkhira&country=Bangladesh&method=1"
                val response = URL(urlString).readText()
                val jsonObject = JSONObject(response)
                
                val data = jsonObject.getJSONObject("data")
                val timings = data.getJSONObject("timings")

                // এপিআই থেকে প্রাপ্ত মূল ওয়াক্তের টাইমগুলো ম্যাপে রিটার্ন করা
                mapOf(
                    "Fajr" to timings.getString("Fajr"),
                    "Sunrise" to timings.getString("Sunrise"),
                    "Dhuhr" to timings.getString("Dhuhr"),
                    "Asr" to timings.getString("Asr"),
                    "Maghrib" to timings.getString("Maghrib"),
                    "Isha" to timings.getString("Isha")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null // কোনো সমস্যা হলে null রিটার্ন করবে
            }
        }
    }
}
