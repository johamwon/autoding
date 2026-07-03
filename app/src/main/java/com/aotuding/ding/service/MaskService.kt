package com.aotuding.ding.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.aotuding.ding.R
import com.aotuding.ding.core.StateProvider
import java.text.SimpleDateFormat
import java.util.*

/**
 * 伪灭屏服务 - 实现简单全屏蒙层 + 时钟 (对标原 repo MaskViewController)
 */
class MaskService : Service() {

    private var windowManager: WindowManager? = null
    private var maskView: View? = null
    private var clockView: TextView? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        StateProvider.MaskState.isMaskActive = true
        showMask()
        return START_STICKY
    }

    private fun showMask() {
        if (maskView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        maskView = LayoutInflater.from(this).inflate(R.layout.mask_overlay, null)

        clockView = maskView?.findViewById(R.id.clockView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager?.addView(maskView, params)
            updateClock()
        } catch (e: Exception) {
            // Permission issue
        }
    }

    private fun updateClock() {
        clockView?.let { tv ->
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tv.text = sdf.format(Date())
            // Simple animation like original (random position would require more code)
        }
    }

    override fun onDestroy() {
        StateProvider.MaskState.isMaskActive = false
        maskView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        maskView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}