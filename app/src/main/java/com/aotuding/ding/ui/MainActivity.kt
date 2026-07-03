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
import com.aotuding.ding.data.repository.TaskRepository
import com.aotuding.ding.databinding.ActivityMainBinding
import com.aotuding.ding.service.CaptureService
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

        // Show current target
        val target = ConfigManager.getTargetApp(this)
        binding.tvStatus.text = "目标: ${target.displayName} | 就绪\n请通过QQ消息配置任务"
    }

    private fun setupRecycler() {
        adapter = TaskAdapter(emptyList())
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
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
            // Simple demo add
            scope.launch {
                TaskRepository.addTask("08:50:00")
                refreshTasks()
                Toast.makeText(this@MainActivity, "已添加 08:50:00 (用QQ消息可更灵活)", Toast.LENGTH_SHORT).show()
            }
        }

        // Long press to request screenshot permission
        binding.btnAddTask.setOnLongClickListener {
            requestMediaProjection()
            Toast.makeText(this, "请授权截屏权限", Toast.LENGTH_SHORT).show()
            true
        }
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
        if (!isNotificationListenerEnabled()) {
            binding.tvStatus.append("\n重要：请在系统设置 > 特殊应用权限 > 通知使用权 中开启「凹凸钉」")
            // Optionally open the settings
            // startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
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