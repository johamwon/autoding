package com.aotuding.ding.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aotuding.ding.core.TaskScheduler

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.aotuding.ding.ACTION_PUNCH") {
            val taskId = intent.getIntExtra("task_id", -1)
            val time = intent.getStringExtra("task_time") ?: ""
            TaskScheduler.onAlarmTriggered(context, taskId, time)
        }
    }
}