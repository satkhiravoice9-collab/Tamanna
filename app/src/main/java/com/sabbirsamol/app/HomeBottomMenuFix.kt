package com.sabbirsamol.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

class HomeBottomMenuFix : Application() {
    
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                // ভিউ ট্রি পুরোপুরি রেন্ডার হওয়ার জন্য ছোট একটি ডিলে দেওয়া হয়েছে
                activity.window.decorView.postDelayed({ fixBottomMenu(activity) }, 100)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    private fun fixBottomMenu(activity: Activity) {
        val root = activity.window.decorView.findViewById<View>(android.R.id.content) ?: return
        val menu = findSevenButtonMenu(root) ?: return
        
        // ডাবল মডিফিকেশন রোধ করার জন্য ট্যাগ চেক
        if (menu.tag == "fixed_bottom_menu_v1") return

        menu.tag = "fixed_bottom_menu_v1"
        menu.orientation = LinearLayout.HORIZONTAL
        menu.gravity = Gravity.CENTER
        menu.setPadding(2, 3, 2, 4)
        menu.minimumHeight = dp(activity, 70)

        val menuItems = listOf(
            "🏠\nহোম",
            "📿\nতাসবিহ",
            "📚\nলাইব্রেরী",
            "🤲\nমাসনুন আমল",
            "📝\nনোটপ্যাড",
            "🔄\nRefresh",
            "ℹ️\nAbout"
        )

        for (i in 0 until menu.childCount) {
            val child = menu.getChildAt(i)
            if (child is Button) {
                child.text = menuItems[i]
                child.textSize = 10f
                child.gravity = Gravity.CENTER
                child.isAllCaps = false
                child.setPadding(0, 2, 0, 2)
                child.minHeight = 0
                child.minimumHeight = 0
                child.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                    setMargins(1, 0, 1, 0)
                }
            }
        }

        val parent = menu.parent as? ViewGroup
        if (parent != null) {
            menu.layoutParams = menu.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = dp(activity, 72)
            }
        }
    }

    private fun findSevenButtonMenu(view: View): LinearLayout? {
        if (view is LinearLayout && view.childCount == 7 &&
            (0 until view.childCount).all { view.getChildAt(it) is Button }) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findSevenButtonMenu(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private fun dp(activity: Activity, value: Int): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }
}
