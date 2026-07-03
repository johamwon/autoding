package com.aotuding.ding.service

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import com.aotuding.ding.utils.MessageSender

/**
 * 截屏服务
 * 需从 Activity 请求权限后启动
 */
class CaptureService : Service() {

    companion object {
        private var active = false
        private var mediaProjection: android.media.projection.MediaProjection? = null

        fun isActive(): Boolean = active

        fun requestCapture() {
            // In production, use a broadcast or event to ask MainActivity to start permission flow
            Log.d("CaptureService", "Capture requested - trigger permission from UI")
        }

        fun setActive(value: Boolean) {
            active = value
        }

        // Call this after user grants permission
        fun onProjectionGranted(activity: Activity, resultCode: Int, data: Intent) {
            val mpm = activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mpm.getMediaProjection(resultCode, data)
            setActive(true)
            // Start actual screen capture here if needed
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setActive(true)
        return START_STICKY
    }

    override fun onDestroy() {
        setActive(false)
        mediaProjection?.stop()
        mediaProjection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}