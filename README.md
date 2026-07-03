# 凹凸钉 (AotuDing)

全新 Android 钉钉自动打卡 Agent App。

## 功能亮点
- 通过 QQ/微信小号消息**完全配置**打卡任务（添加、修改、随机、目标切换、节假日等）
- **实时手机状态识别**：屏幕亮灭、伪灭屏、截屏服务、悬浮窗、任务等
- 安全打卡：仅启动目标 App（依赖极速打卡），无 ADB、无障碍、无位置修改
- 随机时间、伪灭屏、截屏反馈、节假日跳过
- 纯手动代码，无 LLM

## 技术栈
Kotlin + AndroidX + Room + Coroutines + AlarmManager
轻量 XML 界面，不使用 Compose。

## 构建与安装
1. Android Studio 打开 AotuDing 文件夹
2. Gradle Sync
3. 构建 Debug/Release APK
4. 安装到备用手机

**最小 SDK 26 (Android 8)**

## 必须权限（App启动时引导 + 手动）
- 悬浮窗 (Overlay)
- 通知权限 (Android 13+)
- 精确闹钟 (Android 12+)
- 通知监听服务（手动在系统设置 > 特殊应用权限 > 通知使用权 开启「凹凸钉」）
- 截屏权限 (MediaProjection, App内引导授权)
- 忽略电池优化
- WAKE_LOCK 等

QQ命令依赖通知监听。

## 使用流程
1. 开启所有权限 + 目标 App（钉钉等）内部“极速打卡”
2. 用**小号**向手机发送配置指令，例如：
   - `添加任务 08:50`
   - `添加任务 18:05`
   - `设置随机 范围 3`
   - `设置目标 飞书`
   - `查询状态 详细`
   - `查询屏幕`
   - `查询截屏`
   - `息屏`
   - `执行任务`
   - `开启循环`

3. 反馈通过企业微信 Webhook 返回（需在配置中设置 webhook）

## 核心模块说明
- `CommandParser` : 手动解析 QQ 消息
- `StateProvider` : 手机状态收集（PowerManager + 服务状态）
- `NotificationMonitorService` : 命令监听 + 结果监听
- `TaskScheduler` + `AlarmReceiver` : 调度 + 随机时间
- `PunchExecutor` : 启动目标 App
- `FloatingWindowService` : 倒计时悬浮窗
- `ConfigManager` : 全局设置

## 企业微信配置
发送 `设置通知渠道 0 webhook=你的key` （或在代码中预设）

## 注意
- 手机需常亮或使用伪灭屏
- 本 App 完全独立重写，参考了 DailyTask 的设计思路
- 使用风险自负，仅学习用途

当前版本为完整骨架 + 核心逻辑，已可编译测试基本流程。
