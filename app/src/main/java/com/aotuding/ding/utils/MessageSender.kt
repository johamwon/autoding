package com.aotuding.ding.utils

import android.content.Context
import android.util.Log
import com.aotuding.ding.core.ConfigManager
import com.aotuding.ding.core.Constants
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

object MessageSender {

    fun sendFeedback(title: String, content: String) {
        Log.i("MessageSender", "[$title] $content")

        thread {
            try {
                val ctx = com.aotuding.ding.AotuDingApplication.instance
                val channel = ConfigManager.getNotificationChannel(ctx)
                val webhook = ConfigManager.getWebhook(ctx)

                if (channel == 0 && webhook != null) {
                    // Enterprise WeChat webhook
                    sendToWeCom(webhook, title, content)
                } else {
                    // TODO: Email implementation if needed
                    Log.d("MessageSender", "No webhook configured, only logged")
                }
            } catch (e: Exception) {
                Log.e("MessageSender", "Send failed", e)
            }
        }
    }

    private fun sendToWeCom(webhook: String, title: String, content: String) {
        val url = if (webhook.startsWith("http")) webhook else "${Constants.WX_WEB_HOOK_URL}/cgi-bin/webhook/send?key=$webhook"
        val payload = """
            {
                "msgtype": "text",
                "text": {
                    "content": "【$title】\n$content"
                }
            }
        """.trimIndent()

        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 5000
                readTimeout = 5000
            }

            conn.outputStream.use { os ->
                os.write(payload.toByteArray(StandardCharsets.UTF_8))
            }

            val code = conn.responseCode
            Log.i("MessageSender", "WeCom response: $code")
            conn.disconnect()
        } catch (e: Exception) {
            Log.e("MessageSender", "WeCom send error", e)
        }
    }

    fun sendImageFeedback(context: Context, title: String, imagePath: String) {
        Log.i("MessageSender", "Image feedback: $title $imagePath")
        // For full, would upload image and send rich message. Stub for now.
        sendFeedback(title, "截图已保存: $imagePath (需手动查看或扩展上传)")
    }
}