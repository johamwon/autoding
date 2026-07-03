package com.aotuding.ding.core.model

enum class TargetApp(val packageName: String, val displayName: String) {
    DINGDING("com.alibaba.android.rimet", "钉钉"),
    FEISHU("com.ss.android.lark", "飞书"),
    WEWORK("com.tencent.wework", "企业微信"),
    M3("com.seeyon.cmp", "移动办公M3");

    companion object {
        fun fromPackage(pkg: String): TargetApp = entries.find { it.packageName == pkg } ?: DINGDING
    }
}