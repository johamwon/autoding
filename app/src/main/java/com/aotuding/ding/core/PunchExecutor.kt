package com.aotuding.ding.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * 执行打卡：启动目标应用
 * 完全基于 Intent 启动 Launcher，无点击
 */
object PunchExecutor {

    fun launchTargetApp(context: Context, target: TargetApp = TargetApp.DINGDING) {
        try {
            val pkg = target.packageName
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage(pkg)
            }

            val pm = context.packageManager
            val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }

            if (activities.isNotEmpty()) {
                val info = activities[0]
                intent.component = android.content.ComponentName(
                    info.activityInfo.packageName,
                    info.activityInfo.name
                )
                context.startActivity(intent)
                Log.i("PunchExecutor", "已启动 ${target.displayName}")
            } else {
                Log.w("PunchExecutor", "未找到目标应用 Launcher")
            }
        } catch (e: Exception) {
            Log.e("PunchExecutor", "启动失败", e)
        }
    }
}