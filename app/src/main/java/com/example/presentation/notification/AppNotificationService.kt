package com.example.presentation.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class AppNotificationService : Service() {

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels
        AppNotificationHelper.initChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)
        val isSeller = prefs.getBoolean("is_seller", false)

        if (!userId.isNullOrEmpty()) {
            android.util.Log.d("AppNotificationService", "Starting background observers for user: $userId (Seller: $isSeller)")
            AppNotificationHelper.startListening(applicationContext, userId, isSeller)
        } else {
            android.util.Log.d("AppNotificationService", "No active session. Stopping service.")
            AppNotificationHelper.stopListening()
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        AppNotificationHelper.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
