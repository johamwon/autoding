package com.aotuding.ding.service

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.aotuding.ding.utils.MessageSender
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 截屏服务
 * 需从 Activity 请求权限后启动
 */
class CaptureService : Service() {

    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var mediaProjection: MediaProjection? = null
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private var active = false
        private var mediaProjectionInstance: MediaProjection? = null

        fun isActive(): Boolean = active

        fun requestCapture() {
            // Trigger from UI or command
            Log.d("CaptureService", "Capture requested")
        }

        fun performCapture() {
            // This would require the service instance; in practice use bound or singleton
            // For demo, call from activity or assume
        }

        fun setActive(value: Boolean) {
            active = value
        }

        // Call this after user grants permission from MainActivity
        fun onProjectionGranted(activity: Activity, resultCode: Int, data: Intent) {
            val mpm = activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjectionInstance = mpm.getMediaProjection(resultCode, data)
            setActive(true)
            // Start the service to prepare
            val intent = Intent(activity, CaptureService::class.java)
            activity.startService(intent)
        }

        fun captureIfReady() {
            // Called from command, the instance will handle in service
            Log.d("CaptureService", "Capture if ready requested")
            // If service running with projection, it will capture on next start or use broadcast
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setActive(true)
        mediaProjection = mediaProjectionInstance
        // Auto capture if this start was for capture
        if (mediaProjection != null) {
            captureScreen()
        }
        return START_STICKY
    }

    fun captureScreen() {
        if (mediaProjection == null) {
            MessageSender.sendFeedback("截屏失败", "请先通过UI授予截屏权限")
            return
        }

        try {
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "CaptureDisplay",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface, null, null
            )

            handler.postDelayed({
                val image = imageReader?.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width

                    val bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    image.close()

                    // Save to Pictures
                    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(dir, "aotuding_capture_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png")
                    FileOutputStream(file).use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }

                    MessageSender.sendFeedback("截屏成功", "已保存到: ${file.absolutePath}")
                    bitmap.recycle()
                } else {
                    MessageSender.sendFeedback("截屏失败", "无法获取屏幕图像")
                }
                release()
            }, 500) // small delay for render
        } catch (e: Exception) {
            Log.e("CaptureService", "Capture error", e)
            MessageSender.sendFeedback("截屏失败", e.message ?: "未知错误")
            release()
        }
    }

    private fun release() {
        virtualDisplay?.release()
        imageReader?.close()
        virtualDisplay = null
        imageReader = null
    }

    override fun onDestroy() {
        setActive(false)
        release()
        mediaProjection?.stop()
        mediaProjection = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}