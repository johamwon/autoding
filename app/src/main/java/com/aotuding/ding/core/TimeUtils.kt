package com.aotuding.ding.core

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getToday(): String = dateSdf.format(Date())

    fun parseTime(timeStr: String): Long {
        return try {
            val today = getToday()
            val full = "$today $timeStr"
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(full)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun resolveExecutionTime(baseTime: String, randomEnabled: Boolean, rangeMinutes: Int): String {
        val parts = baseTime.split(":").map { it.toInt() }
        var totalSeconds = parts[0] * 3600 + parts[1] * 60 + (parts.getOrNull(2) ?: 0)

        if (randomEnabled && rangeMinutes > 0) {
            // Deterministic random like original repo (same seed per day)
            val seed = "${getToday()}-$baseTime-$rangeMinutes".hashCode().toLong()
            val random = Random(seed)
            val offsetMin = random.nextInt(rangeMinutes + 1)
            val offsetSec = random.nextInt(60)
            totalSeconds += offsetMin * 60 + offsetSec
            if (totalSeconds > 86399) totalSeconds = 86399
        }

        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    fun getCurrentTimeStr(): String = sdf.format(Date())

    fun isHoliday(): Boolean {
        // Simple placeholder. In real: use calendar or hard-coded 2026 holidays
        // For now return false. User can enhance.
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        // Example: skip some known
        return (month == 1 && day == 1) || (month == 10 && day == 1)
    }
}