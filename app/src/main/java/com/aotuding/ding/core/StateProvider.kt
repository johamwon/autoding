package com.aotuding.ding.core

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager
import android.view.Display
import com.aotuding.ding.service.CaptureService
import com.aotuding.ding.service.FloatingWindowService
import com.aotuding.ding.service.NotificationMonitorService

/**
 * 手机当前状态识别
 * 手动实现，无黑盒
 */
object StateProvider {

    fun getFullStatus(context: Context): String {
        val sb = StringBuilder()
        sb.appendLine("=== 凹凸钉 状态报告 ===")
        sb.appendLine("屏幕: ${getScreenState(context)}")
        sb.appendLine("伪灭屏: ${getMaskState()}")
        sb.appendLine("截屏服务: ${getScreenshotState()}")
        sb.appendLine("悬浮窗: ${getFloatingState()}")
        sb.appendLine("通知监听: ${getNotificationListenerState()}")
        sb.appendLine("电量: ${getBattery(context)}%")
        sb.appendLine("任务状态: ${getTaskStatus()}")
        sb.appendLine("前台应用: ${getForegroundApp(context)}")
        sb.appendLine("版本: 1.0.0")
        return sb.toString()
    }

    fun getScreenState(context: Context): String {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = pm.isInteractive
        // 进一步检测显示状态
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        val state = dm.getDisplay(Display.DEFAULT_DISPLAY).state
        val displayOn = state == Display.STATE_ON || state == Display.STATE_ON_SUSPEND
        return if (isInteractive && displayOn) "亮屏" else "息屏"
    }

    fun getMaskState(): String {
        // 由 MaskService 维护状态，这里提供接口
        return if (MaskState.isMaskActive) "已开启" else "未开启"
    }

    fun getScreenshotState(): String {
        return if (CaptureService.isActive()) "正常" else "未授权/已断开"
    }

    fun getFloatingState(): String {
        return if (FloatingWindowService.isRunning) "运行中" else "未运行"
    }

    fun getNotificationListenerState(): String {
        return if (NotificationMonitorService.isConnected) "已连接" else "未连接"
    }

    fun getBattery(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getTaskStatus(): String {
        // 由 TaskScheduler 提供
        return TaskState.currentStatus
    }

    fun getForegroundApp(context: Context): String {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = am.getRunningTasks(1)
            if (tasks.isNotEmpty()) {
                tasks[0].topActivity?.packageName ?: "未知"
            } else "未知"
        } catch (e: Exception) {
            "受限"
        }
    }

    // 供其他模块更新状态
    object MaskState {
        var isMaskActive: Boolean = false
    }

    object TaskState {
        var currentStatus: String = "空闲"
            set(value) {
                field = value
            }
    }
}