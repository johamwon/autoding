package com.aotuding.ding.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.aotuding.ding.data.repository.TaskRepository
import com.aotuding.ding.receiver.AlarmReceiver
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * 任务调度器
 * 使用 AlarmManager 实现每日任务 + 随机时间
 */
object TaskScheduler {

    private const val TAG = "TaskScheduler"
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var currentTasks: List<com.aotuding.ding.data.db.TaskEntity> = emptyList()

    fun start(context: Context) {
        if (isRunning) {
            Log.d(TAG, "Already running")
            return
        }
        isRunning = true
        StateProvider.TaskState.currentStatus = "运行中"

        // Load tasks
        runBlocking {
            currentTasks = TaskRepository.getAllTasks()
        }

        if (currentTasks.isEmpty()) {
            MessageSender.sendFeedback("错误", "无任务，请先添加")
            return
        }

        // Schedule all for today
        scheduleAllForToday(context)

        Log.i(TAG, "Scheduler started with ${currentTasks.size} tasks")
    }

    fun stop() {
        isRunning = false
        StateProvider.TaskState.currentStatus = "已停止"
        // Cancel alarms would need tracking pending intents in real app
        handler.removeCallbacksAndMessages(null)
        Log.i(TAG, "Scheduler stopped")
    }

    fun isRunning() = isRunning

    fun executeNext(context: Context) {
        // Called after success or timeout
        if (!isRunning) return
        // In full version: advance to next task in list
        Log.i(TAG, "Execute next triggered")
        // For demo, could re-schedule or just log
    }

    private fun scheduleAllForToday(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        runBlocking {
            currentTasks = TaskRepository.getAllTasks()
        }

        currentTasks.forEachIndexed { idx, task ->
            val randomEnabled = ConfigManager.isRandomEnabled(context)
            val range = ConfigManager.getRandomRange(context)
            val execTimeStr = TimeUtils.resolveExecutionTime(task.time, randomEnabled, range)
            val execTime = TimeUtils.parseTime(execTimeStr)

            if (execTime > now) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = "com.aotuding.ding.ACTION_PUNCH"
                    putExtra("task_id", task.id)
                    putExtra("task_time", execTimeStr)
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    task.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        execTime,
                        pi
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        execTime,
                        pi
                    )
                }
                Log.i(TAG, "Scheduled task ${task.id} at $execTimeStr")
            }
        }
    }

    // Called from AlarmReceiver
    fun onAlarmTriggered(context: Context, taskId: Int, time: String) {
        if (!isRunning) return

        Log.i(TAG, "Alarm for task $taskId at $time")

        // Check holiday skip
        if (ConfigManager.isSkipHoliday(context) && TimeUtils.isHoliday()) {
            MessageSender.sendFeedback("跳过", "今日为节假日，跳过打卡")
            return
        }

        // Launch the target app
        val target = ConfigManager.getTargetApp(context)
        PunchExecutor.launchTargetApp(context, target)

        // Start countdown overlay etc.
        // In real: start CountdownService with timeout from ConfigManager

        StateProvider.TaskState.currentStatus = "执行中: $time"
    }
}