package com.example.kidmathlocker

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvProtectionStatus: TextView
    private lateinit var switchProtection: SwitchCompat
    private lateinit var btnOverlayPermission: TextView
    private lateinit var btnUsagePermission: TextView
    private lateinit var btnMinus: Button
    private lateinit var btnPlus: Button
    private lateinit var tvDurationDisplay: TextView
    private lateinit var btnGoToAppList: LinearLayout
    private lateinit var tvLockedCount: TextView

    private var currentDurationMinutes = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvProtectionStatus = findViewById(R.id.tvProtectionStatus)
        switchProtection = findViewById(R.id.switchProtection)
        btnOverlayPermission = findViewById(R.id.btnOverlayPermission)
        btnUsagePermission = findViewById(R.id.btnUsagePermission)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        tvDurationDisplay = findViewById(R.id.tvDurationDisplay)
        btnGoToAppList = findViewById(R.id.btnGoToAppList)
        tvLockedCount = findViewById(R.id.tvLockedCount)

        btnOverlayPermission.setOnClickListener { openOverlaySettings() }
        btnUsagePermission.setOnClickListener { openUsageAccessSettings() }

        // Open Screen 2 (App Selection)
        btnGoToAppList.setOnClickListener {
            val intent = Intent(this, AppSelectActivity::class.java)
            startActivity(intent)
        }

        setupDurationControls()
        setupProtectionSwitch()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionIndicators()
        updateLockedCountDisplay()
        updateServiceStatusDisplay()
    }

    private fun setupDurationControls() {
        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        currentDurationMinutes = prefs.getInt("session_duration_minutes", 15)
        tvDurationDisplay.text = "${currentDurationMinutes}m"

        btnPlus.setOnClickListener {
            currentDurationMinutes += 5
            saveDuration()
        }

        btnMinus.setOnClickListener {
            if (currentDurationMinutes > 5) {
                currentDurationMinutes -= 5
            } else if (currentDurationMinutes > 1) {
                currentDurationMinutes -= 1
            }
            saveDuration()
        }
    }

    private fun saveDuration() {
        tvDurationDisplay.text = "${currentDurationMinutes}m"
        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        prefs.edit().putInt("session_duration_minutes", currentDurationMinutes).apply()
    }

    private fun setupProtectionSwitch() {
        switchProtection.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasOverlayPermission() && hasUsageStatsPermission()) {
                    startService(Intent(this, AppMonitorService::class.java))
                    tvProtectionStatus.text = "Active & running in background"
                    tvProtectionStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
                } else {
                    // Cannot enable without permissions
                    switchProtection.isChecked = false
                    tvProtectionStatus.text = "Grant required permissions first"
                    tvProtectionStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_amber))
                }
            } else {
                stopService(Intent(this, AppMonitorService::class.java))
                tvProtectionStatus.text = "Service paused"
                tvProtectionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun updateServiceStatusDisplay() {
        val running = isServiceRunning(AppMonitorService::class.java)
        switchProtection.setOnCheckedChangeListener(null)
        switchProtection.isChecked = running
        setupProtectionSwitch()

        if (running) {
            tvProtectionStatus.text = "Active & running in background"
            tvProtectionStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
        } else {
            tvProtectionStatus.text = "Service paused"
            tvProtectionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateLockedCountDisplay() {
        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        val count = prefs.getStringSet("locked_packages", emptySet())?.size ?: 0
        tvLockedCount.text = "$count app(s) locked"
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun updatePermissionIndicators() {
        val overlay = hasOverlayPermission()
        val usage = hasUsageStatsPermission()

        btnOverlayPermission.text = if (overlay) "✓ Overlay Ready" else "✕ Overlay Needed"
        btnOverlayPermission.setTextColor(
            ContextCompat.getColor(this, if (overlay) R.color.accent_green else R.color.accent_amber)
        )

        btnUsagePermission.text = if (usage) "✓ Usage Ready" else "✕ Usage Needed"
        btnUsagePermission.setTextColor(
            ContextCompat.getColor(this, if (usage) R.color.accent_green else R.color.accent_amber)
        )
    }

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}