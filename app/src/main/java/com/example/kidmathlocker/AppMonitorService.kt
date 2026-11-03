package com.example.kidmathlocker

import android.app.Notification
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class AppMonitorService : Service() {

    private val notificationId = 1001
    private val checkIntervalMs = 700L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var monitorRunnable: Runnable

    private var activeForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        startForegroundServiceNotification()
        initWatcher()
    }

    private fun startForegroundServiceNotification() {
        val notification: Notification = NotificationCompat.Builder(this)
            .setContentTitle("Kid Math Locker")
            .setContentText("Monitoring locked apps")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(notificationId, notification)
    }

    private fun initWatcher() {
        monitorRunnable = Runnable {
            checkForegroundApp()
            handler.postDelayed(monitorRunnable, checkIntervalMs)
        }
        handler.post(monitorRunnable)
    }

    private fun checkForegroundApp() {
        val time = System.currentTimeMillis()
        // Query events from the last 2 seconds
        val events = usageStatsManager.queryEvents(time - 2000, time)
        val event = UsageEvents.Event()
        var latestForegroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latestForegroundPackage = event.packageName
            }
        }

        val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)
        val lockedApps = prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()

        // Track changes in foreground apps
        if (latestForegroundPackage != null) {
            // If the user navigated away from a locked game to a different app (not our lock activity), reset session
            if (activeForegroundPackage != null &&
                activeForegroundPackage != latestForegroundPackage &&
                lockedApps.contains(activeForegroundPackage) &&
                latestForegroundPackage != packageName
            ) {
                prefs.edit().remove("session_expiry_$activeForegroundPackage").apply()
            }

            // Do not overwrite active tracking if the current top app is our own lock screen
            if (latestForegroundPackage != packageName) {
                activeForegroundPackage = latestForegroundPackage
            }
        }

        // Check if the current foreground app requires locking
        val currentApp = activeForegroundPackage
        if (currentApp != null && lockedApps.contains(currentApp)) {
            val unlockExpiration = prefs.getLong("session_expiry_$currentApp", 0L)

            // Intercept if expired or session never granted
            if (System.currentTimeMillis() > unlockExpiration) {
                val lockIntent = Intent(this, MathLockActivity::class.java).apply {
                    putExtra("TARGET_PACKAGE", currentApp)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(lockIntent)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(monitorRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}