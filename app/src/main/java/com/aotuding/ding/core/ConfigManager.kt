package com.aotuding.ding.core

import android.content.Context
import android.content.SharedPreferences
import com.aotuding.ding.core.model.TargetApp

/**
 * 全局配置管理（SharedPreferences）
 */
object ConfigManager {

    private const val PREFS = Constants.PREFS_NAME

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTargetApp(context: Context): TargetApp {
        val idx = prefs(context).getInt(Constants.KEY_TARGET_APP, 0)
        return when (idx) {
            0 -> TargetApp.DINGDING
            1 -> TargetApp.FEISHU
            2 -> TargetApp.WEWORK
            3 -> TargetApp.M3
            else -> TargetApp.DINGDING
        }
    }

    fun setTargetApp(context: Context, target: TargetApp) {
        val idx = when (target) {
            TargetApp.DINGDING -> 0
            TargetApp.FEISHU -> 1
            TargetApp.WEWORK -> 2
            TargetApp.M3 -> 3
        }
        prefs(context).edit().putInt(Constants.KEY_TARGET_APP, idx).apply()
    }

    fun isRandomEnabled(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_RANDOM_ENABLED, true)

    fun setRandomEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_RANDOM_ENABLED, enabled).apply()
    }

    fun getRandomRange(context: Context): Int =
        prefs(context).getInt(Constants.KEY_RANDOM_RANGE, Constants.DEFAULT_RANDOM_MINUTES)

    fun setRandomRange(context: Context, minutes: Int) {
        prefs(context).edit().putInt(Constants.KEY_RANDOM_RANGE, minutes).apply()
    }

    fun getTimeoutSeconds(context: Context): Int =
        prefs(context).getInt(Constants.KEY_TIMEOUT, Constants.DEFAULT_TIMEOUT_SECONDS)

    fun setTimeoutSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(Constants.KEY_TIMEOUT, seconds).apply()
    }

    fun isSkipHoliday(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_SKIP_HOLIDAY, false)

    fun setSkipHoliday(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_SKIP_HOLIDAY, enabled).apply()
    }

    fun getNotificationChannel(context: Context): Int =
        prefs(context).getInt(Constants.KEY_NOTIFICATION_CHANNEL, 0)

    fun setNotificationChannel(context: Context, channel: Int) {
        prefs(context).edit().putInt(Constants.KEY_NOTIFICATION_CHANNEL, channel).apply()
    }

    fun getWebhook(context: Context): String? =
        prefs(context).getString(Constants.KEY_WEBHOOK, null)

    fun setWebhook(context: Context, webhook: String?) {
        prefs(context).edit().putString(Constants.KEY_WEBHOOK, webhook).apply()
    }

    fun getResetTime(context: Context): String =
        prefs(context).getString("reset_time", "00:00:00") ?: "00:00:00"

    fun setResetTime(context: Context, time: String) {
        prefs(context).edit().putString("reset_time", time).apply()
    }
}