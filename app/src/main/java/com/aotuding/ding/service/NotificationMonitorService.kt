package com.aotuding.ding.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.aotuding.ding.core.CommandParser
import com.aotuding.ding.core.ConfigManager
import com.aotuding.ding.core.Constants
import com.aotuding.ding.core.StateProvider
import com.aotuding.ding.core.TaskScheduler
import com.aotuding.ding.core.model.Action
import com.aotuding.ding.data.repository.TaskRepository
import com.aotuding.ding.utils.MessageSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationMonitorService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        var isConnected = false
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        Log.d("NMS", "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        requestRebind()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        val extras = sbn.notification.extras
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val title = extras.getString("android.title") ?: ""

        // Target app result (e.g. DingTalk success)
        val targetPkg = ConfigManager.getTargetApp(this).packageName
        if (pkg == targetPkg && (text.contains("成功") || text.contains("打卡"))) {
            scope.launch {
                val status = StateProvider.getFullStatus(this@NotificationMonitorService)
                MessageSender.sendFeedback(
                    Constants.FEEDBACK_TITLE_TASK,
                    "打卡结果: $text\n$status"
                )
                // Optionally trigger next task
                TaskScheduler.executeNext(this@NotificationMonitorService)
            }
            return
        }

        // Command messages from QQ/WeChat etc.
        if (pkg in Constants.COMMAND_PACKAGES) {
            handleCommand(text)
        }
    }

    private fun handleCommand(raw: String) {
        val action = CommandParser.parse(raw) ?: return

        scope.launch {
            try {
                when (action) {
                    is Action.AddTask -> {
                        TaskRepository.addTask(action.time)
                        MessageSender.sendFeedback("配置成功", "已添加任务: ${action.time}")
                    }
                    is Action.ModifyTask -> {
                        TaskRepository.updateTask(action.index, action.time)
                        MessageSender.sendFeedback("配置成功", "修改任务 ${action.index + 1} 为 ${action.time}")
                    }
                    is Action.DeleteTask -> {
                        TaskRepository.deleteTask(action.index)
                        MessageSender.sendFeedback("配置成功", "删除任务 ${action.index + 1}")
                    }
                    is Action.ClearTasks -> {
                        TaskRepository.clearAll()
                        MessageSender.sendFeedback("配置成功", "已清空所有任务")
                    }
                    is Action.ListTasks -> {
                        val tasks = TaskRepository.getAllTasks()
                        val list = tasks.joinToString("\n") { "${it.id}: ${it.time}" }
                        MessageSender.sendFeedback("任务列表", list.ifEmpty { "无任务" })
                    }

                    is Action.SetTarget -> {
                        ConfigManager.setTargetApp(this@NotificationMonitorService, action.target)
                        MessageSender.sendFeedback("配置成功", "目标应用: ${action.target.displayName}")
                    }
                    is Action.SetRandom -> {
                        ConfigManager.setRandomEnabled(this@NotificationMonitorService, action.enabled)
                        ConfigManager.setRandomRange(this@NotificationMonitorService, action.rangeMinutes)
                        MessageSender.sendFeedback(
                            "配置成功",
                            "随机: ${if (action.enabled) "开启" else "关闭"}, 范围 ${action.rangeMinutes}分钟"
                        )
                    }
                    is Action.SetTimeout -> {
                        ConfigManager.setTimeoutSeconds(this@NotificationMonitorService, action.seconds)
                        MessageSender.sendFeedback("配置成功", "超时设置为 ${action.seconds}秒")
                    }
                    is Action.SetResetTime -> {
                        ConfigManager.setResetTime(this@NotificationMonitorService, action.time)
                        MessageSender.sendFeedback("配置成功", "重置时间: ${action.time}")
                    }
                    is Action.SetSkipHoliday -> {
                        ConfigManager.setSkipHoliday(this@NotificationMonitorService, action.enabled)
                        MessageSender.sendFeedback("配置成功", "节假日跳过: ${if (action.enabled) "开启" else "关闭"}")
                    }
                    is Action.SetNotification -> {
                        ConfigManager.setNotificationChannel(this@NotificationMonitorService, action.channel)
                        action.webhook?.let { ConfigManager.setWebhook(this@NotificationMonitorService, it) }
                        MessageSender.sendFeedback("配置成功", "通知渠道已更新")
                    }

                    is Action.ExecuteTask -> {
                        TaskScheduler.start(this@NotificationMonitorService)
                        MessageSender.sendFeedback("指令", "已启动任务执行")
                    }
                    is Action.StopTask -> {
                        TaskScheduler.stop()
                        MessageSender.sendFeedback("指令", "已终止当天任务")
                    }
                    is Action.EnableLoop -> {
                        // Could set a flag, for now just start
                        TaskScheduler.start(this@NotificationMonitorService)
                        MessageSender.sendFeedback("指令", "循环已开启")
                    }
                    is Action.DisableLoop -> {
                        TaskScheduler.stop()
                        MessageSender.sendFeedback("指令", "循环已关闭")
                    }
                    is Action.EnableMask -> {
                        // Trigger mask (will be handled by dedicated service or Main)
                        StateProvider.MaskState.isMaskActive = true
                        MessageSender.sendFeedback("指令", "伪灭屏已开启")
                    }
                    is Action.DisableMask -> {
                        StateProvider.MaskState.isMaskActive = false
                        MessageSender.sendFeedback("指令", "伪灭屏已关闭")
                    }
                    is Action.CaptureScreen -> {
                        CaptureService.requestCapture()
                        MessageSender.sendFeedback("指令", "截屏请求已发送")
                    }

                    is Action.QueryStatus -> {
                        val status = StateProvider.getFullStatus(this@NotificationMonitorService)
                        MessageSender.sendFeedback(Constants.FEEDBACK_TITLE_STATUS, status)
                    }
                    is Action.QueryDetailedStatus -> {
                        val status = StateProvider.getFullStatus(this@NotificationMonitorService)
                        MessageSender.sendFeedback(Constants.FEEDBACK_TITLE_STATUS, "详细:\n$status")
                    }
                    is Action.QueryScreen -> {
                        val s = StateProvider.getScreenState(this@NotificationMonitorService)
                        MessageSender.sendFeedback("屏幕状态", s)
                    }
                    is Action.QueryScreenshot -> {
                        val s = StateProvider.getScreenshotState()
                        MessageSender.sendFeedback("截屏服务", s)
                    }
                    is Action.QueryFloating -> {
                        val s = StateProvider.getFloatingState()
                        MessageSender.sendFeedback("悬浮窗", s)
                    }
                    is Action.AttendanceRecord -> {
                        // Could query logs later
                        MessageSender.sendFeedback("考勤记录", "最近记录请查看日志")
                    }
                    is Action.Unknown -> {
                        MessageSender.sendFeedback("未知指令", action.reason)
                    }
                }
            } catch (e: Exception) {
                MessageSender.sendFeedback("错误", "处理指令失败: ${e.message}")
            }
        }
    }
}