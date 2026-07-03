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
            // Optionally auto start scheduler if loop was enabled
            // TaskScheduler.start(context)
        }
    }
}