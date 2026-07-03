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

class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var tvCountdown: TextView? = null
    private var tvStatus: TextView? = null

    companion object {
        var isRunning = false
            private set

        // Simple static update for demo (in production use bound service or LocalBroadcast)
        fun updateCountdown(seconds: Int, status: String) {
            // This is placeholder; actual update happens inside service instance
            // For full impl, bind or use broadcast receiver inside FloatingWindowService
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 100
        }

        tvCountdown = floatingView?.findViewById(R.id.tvCountdown)
        tvStatus = floatingView?.findViewById(R.id.tvStatus)

        try {
            windowManager?.addView(floatingView, params)
            isRunning = true
        } catch (e: Exception) {
            // Permission not granted
        }
    }

    fun updateCountdown(seconds: Int, status: String) {
        tvCountdown?.text = "${seconds}s"
        tvStatus?.text = status
    }

    fun hide() {
        floatingView?.visibility = View.GONE
    }

    fun show() {
        floatingView?.visibility = View.VISIBLE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val secs = it.getIntExtra("seconds", -1)
            val status = it.getStringExtra("status") ?: "执行中"
            if (secs >= 0) {
                updateCountdown(secs, status)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}