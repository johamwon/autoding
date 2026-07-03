package com.aotuding.ding.core.model

sealed class Action {
    data class AddTask(val time: String) : Action()
    data class ModifyTask(val index: Int, val time: String) : Action()
    data class DeleteTask(val index: Int) : Action()
    object ClearTasks : Action()
    object ListTasks : Action()

    data class SetTarget(val target: TargetApp) : Action()
    data class SetRandom(val enabled: Boolean, val rangeMinutes: Int) : Action()
    data class SetTimeout(val seconds: Int) : Action()
    data class SetResetTime(val time: String) : Action()
    data class SetSkipHoliday(val enabled: Boolean) : Action()
    data class SetNotification(val channel: Int, val webhook: String?) : Action()

    object ExecuteTask : Action()
    object StopTask : Action()
    object EnableLoop : Action()
    object DisableLoop : Action()
    object EnableMask : Action()
    object DisableMask : Action()
    object CaptureScreen : Action()

    object QueryStatus : Action()
    object QueryDetailedStatus : Action()
    object QueryScreen : Action()
    object QueryScreenshot : Action()
    object QueryFloating : Action()
    object AttendanceRecord : Action()

    data class Unknown(val reason: String) : Action()
}