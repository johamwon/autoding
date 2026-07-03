package com.aotuding.ding.core

object Constants {
    // Target apps
    const val TARGET_DINGDING = "com.alibaba.android.rimet"
    const val TARGET_FEISHU = "com.ss.android.lark"
    const val TARGET_WEWORK = "com.tencent.wework"
    const val TARGET_M3 = "com.seeyon.cmp"

    // Command source packages (listen for config)
    val COMMAND_PACKAGES = listOf(
        "com.tencent.mobileqq",   // QQ
        "com.tencent.mm",         // WeChat
        "com.tencent.tim",        // TIM
        "com.eg.android.AlipayGphone" // Alipay
    )

    // Default values
    const val DEFAULT_TIMEOUT_SECONDS = 30
    const val DEFAULT_RANDOM_MINUTES = 5
    const val DEFAULT_RESET_HOUR = 0

    // Keys for SharedPreferences (simple config)
    const val PREFS_NAME = "aotuding_prefs"
    const val KEY_TARGET_APP = "key_target_app"
    const val KEY_RANDOM_ENABLED = "key_random_enabled"
    const val KEY_RANDOM_RANGE = "key_random_range"
    const val KEY_TIMEOUT = "key_timeout"
    const val KEY_SKIP_HOLIDAY = "key_skip_holiday"
    const val KEY_NOTIFICATION_CHANNEL = "key_notification_channel"
    const val KEY_WEBHOOK = "key_webhook"

    // Notification channels
    const val CHANNEL_ID_FOREGROUND = "aotuding_foreground"
    const val CHANNEL_ID_COUNTDOWN = "aotuding_countdown"

    // Feedback titles
    const val FEEDBACK_TITLE_STATUS = "凹凸钉状态"
    const val FEEDBACK_TITLE_TASK = "凹凸钉任务"

    // WeCom
    const val WX_WEB_HOOK_URL = "https://qyapi.weixin.qq.com"
}