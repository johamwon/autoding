package com.aotuding.ding.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aotuding.ding.core.TaskScheduler
import com.aotuding.ding.service.ForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Auto start keepalive
            context.startService(Intent(context, ForegroundService::class.java))
            // Check if need daily reset and restart scheduler (like original)
            TaskScheduler.start(context)
            // Reset if past reset time
            TaskScheduler.resetDailyTasks(context)
        }
    }
}