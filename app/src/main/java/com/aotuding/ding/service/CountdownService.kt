package com.aotuding.ding.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.aotuding.ding.core.ConfigManager

/**
 * 倒计时服务，控制浮窗并在结束时返回
 */
class CountdownService : Service() {

    private var timer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeout = ConfigManager.getTimeoutSeconds(this)
        val startFloating = Intent(this, FloatingWindowService::class.java)
        startService(startFloating)

        timer = object : CountDownTimer(timeout * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                // Update floating if running
                // In practice, use EventBus or Binder, here simplified broadcast or direct
            }

            override fun onFinish() {
                // Return to home or main
                // Trigger next logic
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}