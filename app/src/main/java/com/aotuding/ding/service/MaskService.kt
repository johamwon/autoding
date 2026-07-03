package com.aotuding.ding.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import com.aotuding.ding.core.StateProvider

/**
 * 伪灭屏服务 (简单实现，实际可扩展为 Activity overlay)
 */
class MaskService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        StateProvider.MaskState.isMaskActive = true
        // In full version: show full screen dark view with clock
        return START_STICKY
    }

    override fun onDestroy() {
        StateProvider.MaskState.isMaskActive = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}