package com.aotuding.ding.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aotuding.ding.R
import com.aotuding.ding.core.ConfigManager
import com.aotuding.ding.core.TaskScheduler
import com.aotuding.ding.core.model.TargetApp
import com.aotuding.ding.data.repository.TaskRepository
import com.aotuding.ding.databinding.ActivityMainBinding
import com.aotuding.ding.core.PunchExecutor
import com.aotuding.ding.service.CaptureService
import com.aotuding.ding.service.CountdownService
import com.aotuding.ding.service.ForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupButtons()
        startServices()
        refreshTasks()

        checkAndRequestPermissions()
        binding.tvStatus.text = "凹凸钉已启动\n使用上方按钮调整配置，或用QQ发送指令"
        if (!isNotificationListenerEnabled()) {
            binding.tvStatus.append("\n【重要】监听未开启：请去系统设置 > 特殊应用权限 > 通知使用权 开启「凹凸钉」")
        }
        refreshConfigDisplay()
    }

    private fun setupRecycler() {
        adapter = TaskAdapter(emptyList())
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        adapter.setOnTaskClickListener(object : TaskAdapter.OnTaskClickListener {
            override fun onTaskClick(task: com.aotuding.ding.data.db.TaskEntity, position: Int) {
                // Edit task with time picker
                showTimePickerDialog(task, position)
            }

            override fun onTaskLongClick(task: com.aotuding.ding.data.db.TaskEntity, position: Int) {
                // Delete
                scope.launch {
                    TaskRepository.deleteTask(task.id)
                    refreshTasks()
                    refreshConfigDisplay()
                    Toast.makeText(this@MainActivity, "已删除任务 ${task.time}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupButtons() {
        binding.btnToggleTask.setOnClickListener {
            if (TaskScheduler.isRunning()) {
                TaskScheduler.stop()
                binding.btnToggleTask.text = getString(R.string.start_task)
                binding.tvStatus.text = "已停止"
            } else {
                TaskScheduler.start(this)
                binding.btnToggleTask.text = getString(R.string.stop_task)
                binding.tvStatus.text = "任务运行中"
            }
        }

        binding.btnAddTask.setOnClickListener {
            showTimePickerDialog()  // Use time picker like original
        }

        // Long press for screenshot permission
        binding.btnAddTask.setOnLongClickListener {
            requestMediaProjection()
            Toast.makeText(this, "请授权截屏权限", Toast.LENGTH_SHORT).show()
            true
        }

        // New config buttons
        binding.btnSwitchTarget?.setOnClickListener {
            cycleTargetApp()
        }

        binding.btnToggleRandom?.setOnClickListener {
            val enabled = !ConfigManager.isRandomEnabled(this)
            ConfigManager.setRandomEnabled(this, enabled)
            refreshConfigDisplay()
            Toast.makeText(this, "随机: ${if (enabled) "开启" else "关闭"}", Toast.LENGTH_SHORT).show()
        }

        binding.btnSetTimeout?.setOnClickListener {
            val current = ConfigManager.getTimeoutSeconds(this)
            val newTimeout = if (current >= 60) 30 else current + 15
            ConfigManager.setTimeoutSeconds(this, newTimeout)
            refreshConfigDisplay()
            Toast.makeText(this, "超时设为 ${newTimeout}s", Toast.LENGTH_SHORT).show()
        }

        binding.btnToggleHoliday?.setOnClickListener {
            val enabled = !ConfigManager.isSkipHoliday(this)
            ConfigManager.setSkipHoliday(this, enabled)
            refreshConfigDisplay()
            Toast.makeText(this, "节假日跳过: ${if (enabled) "开启" else "关闭"}", Toast.LENGTH_SHORT).show()
        }

        binding.btnResultSource?.setOnClickListener {
            val current = ConfigManager.getResultSource(this)
            val next = if (current == 0) 1 else 0
            ConfigManager.setResultSource(this, next)
            refreshConfigDisplay()
            Toast.makeText(this, "结果来源: ${if (next == 0) "通知监听" else "截屏服务"}", Toast.LENGTH_SHORT).show()
        }

        binding.btnManualExecute?.setOnClickListener {
            // Manual immediate execute for testing
            val target = ConfigManager.getTargetApp(this)
            PunchExecutor.launchTargetApp(this, target)
            val countdownIntent = Intent(this, CountdownService::class.java)
            startService(countdownIntent)
            binding.tvStatus.text = "手动执行中... 请查看悬浮窗"
            Toast.makeText(this, "已手动启动打卡 (目标: ${target.displayName})", Toast.LENGTH_SHORT).show()
        }

        binding.btnRequestPerms?.setOnClickListener {
            checkAndRequestPermissions()
            val listenerOk = isNotificationListenerEnabled()
            binding.tvStatus.append("\n监听状态: ${if (listenerOk) "已开启" else "未开启 (必须手动在系统设置开启)"}")
        }
    }

    private fun cycleTargetApp() {
        val current = ConfigManager.getTargetApp(this)
        val next = when (current) {
            TargetApp.DINGDING -> TargetApp.FEISHU
            TargetApp.FEISHU -> TargetApp.WEWORK
            TargetApp.WEWORK -> TargetApp.M3
            TargetApp.M3 -> TargetApp.DINGDING
        }
        ConfigManager.setTargetApp(this, next)
        refreshConfigDisplay()
        Toast.makeText(this, "目标切换为: ${next.displayName}", Toast.LENGTH_SHORT).show()
    }

    private fun startServices() {
        // Use startForegroundService for foreground services (required on API 26+)
        ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
        // Notification listener is started by system when permission granted
    }

    private fun refreshTasks() {
        scope.launch {
            val tasks = withContext(Dispatchers.IO) { TaskRepository.getAllTasks() }
            adapter.update(tasks)
        }
    }

    private fun checkAndRequestPermissions() {
        // Overlay permission (critical for floating window)
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "请开启悬浮窗权限", Toast.LENGTH_LONG).show()
        }

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(this, "请允许精确闹钟权限用于定时打卡", Toast.LENGTH_LONG).show()
            }
        }

        // Notification Listener - must be enabled manually in system settings
        // (status updated after)
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    override fun onResume() {
        super.onResume()
        refreshTasks()
        refreshConfigDisplay()
    }

    private fun refreshConfigDisplay() {
        val target = ConfigManager.getTargetApp(this)
        val randomEnabled = ConfigManager.isRandomEnabled(this)
        val randomRange = ConfigManager.getRandomRange(this)
        val timeout = ConfigManager.getTimeoutSeconds(this)
        val skipHoliday = ConfigManager.isSkipHoliday(this)
        val resultSource = ConfigManager.getResultSource(this)

        val configText = "目标: ${target.displayName} | 随机: ${if (randomEnabled) "开(${randomRange}min)" else "关"} | 超时: ${timeout}s | 节假日跳过: ${if (skipHoliday) "开" else "关"} | 结果来源: ${if (resultSource == 0) "通知" else "截屏"}"
        binding.tvConfig?.text = configText
    }

    private fun showTimePickerDialog(existingTask: com.aotuding.ding.data.db.TaskEntity? = null, position: Int = -1) {
        val calendar = java.util.Calendar.getInstance()
        if (existingTask != null) {
            val parts = existingTask.time.split(":")
            if (parts.size >= 2) {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
                calendar.set(java.util.Calendar.MINUTE, parts[1].toInt())
            }
        }

        val timePicker = android.app.TimePickerDialog(
            this,
            { _, hour, minute ->
                val timeStr = String.format("%02d:%02d:00", hour, minute)
                scope.launch {
                    if (existingTask != null) {
                        TaskRepository.updateTask(existingTask.id, timeStr)
                        Toast.makeText(this@MainActivity, "已修改为 $timeStr", Toast.LENGTH_SHORT).show()
                    } else {
                        TaskRepository.addTask(timeStr)
                        Toast.makeText(this@MainActivity, "已添加 $timeStr", Toast.LENGTH_SHORT).show()
                    }
                    refreshTasks()
                    refreshConfigDisplay()
                }
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to request media projection (for screenshot)
    fun requestMediaProjection() {
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val intent = mpm.createScreenCaptureIntent()
        startActivityForResult(intent, 1002)
    }

    @Deprecated("Use Activity Result API in modern code")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == RESULT_OK && data != null) {
            // Pass to CaptureService
            CaptureService.onProjectionGranted(this, resultCode, data)
            Toast.makeText(this, "截屏权限已获取，可通过QQ指令截屏", Toast.LENGTH_SHORT).show()
        }
    }
}