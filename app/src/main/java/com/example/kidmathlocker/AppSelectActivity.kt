package com.example.kidmathlocker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppSelectActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvHeaderSubtitle: TextView
    private lateinit var recyclerViewApps: RecyclerView

    private val lockedPackages = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_select)

        btnBack = findViewById(R.id.btnBack)
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle)
        recyclerViewApps = findViewById(R.id.recyclerViewApps)

        btnBack.setOnClickListener { finish() }

        loadLockedPackages()
        setupAppList()
    }

    private fun loadLockedPackages() {
        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        val savedSet = prefs.getStringSet("locked_packages", emptySet())
        if (savedSet != null) {
            lockedPackages.clear()
            lockedPackages.addAll(savedSet)
        }
        updateSubtitle()
    }

    private fun saveLockedPackages() {
        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("locked_packages", HashSet(lockedPackages)).apply()
        updateSubtitle()
    }

    private fun updateSubtitle() {
        tvHeaderSubtitle.text = "${lockedPackages.size} app(s) currently locked"
    }

    private fun setupAppList() {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = pm.queryIntentActivities(mainIntent, 0)
        val appItems = mutableListOf<AppItem>()

        for (resolveInfo in resolveInfoList) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            if (pkg != packageName) {
                val label = resolveInfo.loadLabel(pm)?.toString() ?: pkg
                val icon = resolveInfo.loadIcon(pm) ?: continue
                val isLocked = lockedPackages.contains(pkg)
                appItems.add(AppItem(label, pkg, icon, isLocked))
            }
        }

        appItems.sortBy { it.name.lowercase() }

        recyclerViewApps.layoutManager = LinearLayoutManager(this)
        recyclerViewApps.adapter = AppAdapter(appItems) { item, isChecked ->
            val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
            if (isChecked) {
                lockedPackages.add(item.packageName)
                prefs.edit().remove("session_expiry_${item.packageName}").apply()
            } else {
                lockedPackages.remove(item.packageName)
                prefs.edit().remove("session_expiry_${item.packageName}").apply()
            }
            saveLockedPackages()
        }
    }
}