package com.aotuding.ding.core

import com.aotuding.ding.core.model.Action
import com.aotuding.ding.core.model.TargetApp
import java.util.regex.Pattern

/**
 * 纯手动编码的指令解析器
 * 支持通过QQ消息进行各种配置
 * 无LLM，基于关键词 + 正则
 */
object CommandParser {

    private val timePattern = Pattern.compile("(\\d{1,2})[:点时](\\d{1,2})(?::(\\d{1,2}))?")
    private val numberPattern = Pattern.compile("(\\d+)")

    fun parse(message: String): Action? {
        val text = message.trim().lowercase()

        // 忽略部分语气词，但保留“立即”用于特殊处理
        // Handle prefix conflicts like original (check longer matches first)
        val clean = text.replace("请", "")
            .replace("帮我", "")
            .replace("现在", "")
            .trim()

        return when {
            // 任务管理
            clean.contains("添加任务") || clean.contains("添加") && containsTime(clean) -> parseAddTask(clean)
            clean.contains("修改任务") || clean.contains("改") && containsTime(clean) -> parseModifyTask(clean)
            clean.contains("删除任务") -> parseDeleteTask(clean)
            clean.contains("清空") && clean.contains("任务") -> Action.ClearTasks
            clean.contains("列出任务") || clean.contains("任务列表") -> Action.ListTasks

            // 参数配置
            clean.contains("设置目标") || clean.contains("切换目标") || clean.contains("目标") -> parseTarget(clean)
            clean.contains("随机") -> parseRandom(clean)
            clean.contains("超时") -> parseTimeout(clean)
            clean.contains("重置时间") -> parseResetTime(clean)
            clean.contains("节假日") -> parseHoliday(clean)
            clean.contains("通知渠道") || clean.contains("webhook") -> parseNotification(clean)
            clean.contains("结果来源") -> parseResultSource(clean)
            clean.contains("导出") && (clean.contains("配置") || clean.contains("任务")) -> Action.ExportConfig

            // 控制
            (clean.contains("立即") || clean.contains("马上") || clean.contains("现在") || clean.contains("马上")) && (clean.contains("执行") || clean.contains("打卡") || clean.contains("启动")) -> Action.ExecuteTaskImmediate
            clean.contains("执行任务") || clean.contains("打卡") -> Action.ExecuteTask
            clean.contains("终止任务") || clean.contains("停止任务") -> Action.StopTask
            clean.contains("开启循环") || clean.contains("开始循环") -> Action.EnableLoop
            clean.contains("关闭循环") || clean.contains("暂停循环") -> Action.DisableLoop
            clean.contains("息屏") || clean.contains("伪灭屏") -> Action.EnableMask
            clean.contains("亮屏") -> Action.DisableMask
            clean.contains("截屏") -> Action.CaptureScreen

            // 状态查询
            clean.contains("状态查询") || clean.contains("查询状态") -> parseStateQuery(clean)
            clean.contains("查询屏幕") || clean.contains("屏幕状态") -> Action.QueryScreen
            clean.contains("查询截屏") || clean.contains("截屏服务") -> Action.QueryScreenshot
            clean.contains("查询悬浮") -> Action.QueryFloating
            clean.contains("考勤记录") -> Action.AttendanceRecord

            else -> null
        }
    }

    private fun containsTime(text: String): Boolean = timePattern.matcher(text).find()

    private fun parseAddTask(text: String): Action {
        val time = extractTime(text) ?: "08:50:00"
        return Action.AddTask(time)
    }

    private fun parseModifyTask(text: String): Action {
        val time = extractTime(text) ?: return Action.Unknown("未识别时间")
        val index = extractNumber(text) ?: 1
        return Action.ModifyTask(index - 1, time)
    }

    private fun parseDeleteTask(text: String): Action {
        val index = extractNumber(text) ?: 1
        return Action.DeleteTask(index - 1)
    }

    private fun parseTarget(text: String): Action {
        val target = when {
            text.contains("钉钉") -> TargetApp.DINGDING
            text.contains("飞书") -> TargetApp.FEISHU
            text.contains("企业微信") || text.contains("wework") -> TargetApp.WEWORK
            text.contains("m3") || text.contains("移动办公") -> TargetApp.M3
            else -> TargetApp.DINGDING
        }
        return Action.SetTarget(target)
    }

    private fun parseRandom(text: String): Action {
        val enabled = !text.contains("关闭") && !text.contains("取消")
        val range = extractNumber(text) ?: Constants.DEFAULT_RANDOM_MINUTES
        return Action.SetRandom(enabled, range)
    }

    private fun parseTimeout(text: String): Action {
        val seconds = extractNumber(text) ?: Constants.DEFAULT_TIMEOUT_SECONDS
        return Action.SetTimeout(seconds)
    }

    private fun parseResetTime(text: String): Action {
        val time = extractTime(text) ?: "00:00:00"
        return Action.SetResetTime(time)
    }

    private fun parseHoliday(text: String): Action {
        val enabled = text.contains("开启") || text.contains("打开") || !text.contains("关闭")
        return Action.SetSkipHoliday(enabled)
    }

    private fun parseNotification(text: String): Action {
        val webhook = if (text.contains("webhook")) {
            text.substringAfter("webhook").trim().substringBefore(" ").ifBlank { null }
        } else null
        val channel = if (text.contains("企业微信")) 0 else 1
        return Action.SetNotification(channel, webhook)
    }

    private fun parseResultSource(text: String): Action {
        val source = if (text.contains("截屏") || text.contains("1")) 1 else 0
        return Action.SetResultSource(source)
    }

    private fun parseStateQuery(text: String): Action {
        return if (text.contains("详细")) Action.QueryDetailedStatus else Action.QueryStatus
    }

    private fun extractTime(text: String): String? {
        val matcher = timePattern.matcher(text)
        if (matcher.find()) {
            val h = matcher.group(1)?.padStart(2, '0') ?: "08"
            val m = matcher.group(2)?.padStart(2, '0') ?: "50"
            val s = matcher.group(3)?.padStart(2, '0') ?: "00"
            return "$h:$m:$s"
        }
        return null
    }

    private fun extractNumber(text: String): Int? {
        val matcher = numberPattern.matcher(text)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }
}