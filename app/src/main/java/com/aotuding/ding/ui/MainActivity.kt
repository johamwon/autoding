package com.aotuding.ding.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aotuding.ding.R
import com.aotuding.ding.core.ConfigManager
import com.aotuding.ding.core.PunchExecutor
import com.aotuding.ding.core.TaskScheduler
import com.aotuding.ding.core.model.TargetApp
import com.aotuding.ding.data.repository.TaskRepository
import com.aotuding.ding.databinding.ActivityMainBinding
import com.aotuding.ding.service.ForegroundService
import com.aotuding.ding.service.NotificationMonitorService
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

        // Demo: show current target
        val target = ConfigManager.getTargetApp(this)
        binding.tvStatus.text = "目标: ${target.displayName} | 就绪"
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
    }

    private fun startServices() {
        startService(Intent(this, ForegroundService::class.java))
        // Notification listener is started by system when permission granted
    }

    private fun refreshTasks() {
        scope.launch {
            val tasks = withContext(Dispatchers.IO) { TaskRepository.getAllTasks() }
            adapter.update(tasks)
        }
    }

    private fun checkAndRequestPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "请开启悬浮窗权限", Toast.LENGTH_LONG).show()
        }

        // User must manually enable Notification Listener in Settings > Special app access
        binding.tvStatus.append("\n请在系统设置中开启「通知使用权」给凹凸钉")
    }

    override fun onResume() {
        super.onResume()
        refreshTasks()
    }
}