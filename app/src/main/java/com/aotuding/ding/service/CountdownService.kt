package com.aotuding.ding.service

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.aotuding.ding.core.ConfigManager
import com.aotuding.ding.utils.MessageSender

/**
 * 倒计时服务，控制浮窗并在结束时返回
 */
class CountdownService : Service() {

    private var timer: CountDownTimer? = null
    private var floatingService: FloatingWindowService? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeout = ConfigManager.getTimeoutSeconds(this)
        val startFloating = Intent(this, FloatingWindowService::class.java)
        startService(startFloating)

        timer = object : CountDownTimer(timeout * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                // Update floating via intent
                val updateIntent = Intent(this@CountdownService, FloatingWindowService::class.java).apply {
                    putExtra("seconds", sec)
                    putExtra("status", "打卡中...")
                }
                startService(updateIntent)
            }

            override fun onFinish() {
                // Auto return to home to hide the target app
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)

                // Send timeout feedback
                MessageSender.sendFeedback("打卡超时", "未检测到成功通知，请手动确认")
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