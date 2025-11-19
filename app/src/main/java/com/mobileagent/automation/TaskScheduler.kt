package com.mobileagent.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobileagent.MobileAgentApplication
import java.util.*

data class ScheduledTask(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val command: String,
    val schedule: TaskSchedule,
    val enabled: Boolean = true
)

sealed class TaskSchedule {
    data class Once(val timestamp: Long) : TaskSchedule()
    data class Daily(val hour: Int, val minute: Int) : TaskSchedule()
    data class Weekly(val dayOfWeek: Int, val hour: Int, val minute: Int) : TaskSchedule()
    data class Interval(val intervalMillis: Long) : TaskSchedule()
}

class TaskScheduler {
    private val context = MobileAgentApplication.getAppContext()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val tasks = mutableListOf<ScheduledTask>()

    fun scheduleTask(task: ScheduledTask) {
        tasks.add(task)

        if (task.enabled) {
            val intent = Intent(context, TaskExecutorReceiver::class.java).apply {
                putExtra("TASK_ID", task.id)
                putExtra("COMMAND", task.command)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            when (val schedule = task.schedule) {
                is TaskSchedule.Once -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        schedule.timestamp,
                        pendingIntent
                    )
                }
                is TaskSchedule.Daily -> {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, schedule.hour)
                        set(Calendar.MINUTE, schedule.minute)
                        set(Calendar.SECOND, 0)
                    }

                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
                is TaskSchedule.Weekly -> {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, schedule.dayOfWeek)
                        set(Calendar.HOUR_OF_DAY, schedule.hour)
                        set(Calendar.MINUTE, schedule.minute)
                        set(Calendar.SECOND, 0)
                    }

                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    }

                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                }
                is TaskSchedule.Interval -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + schedule.intervalMillis,
                        schedule.intervalMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelTask(taskId: String) {
        val intent = Intent(context, TaskExecutorReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        tasks.removeAll { it.id == taskId }
    }

    fun getAllTasks(): List<ScheduledTask> = tasks.toList()

    fun getTask(taskId: String): ScheduledTask? = tasks.find { it.id == taskId }

    fun updateTask(task: ScheduledTask) {
        cancelTask(task.id)
        scheduleTask(task)
    }
}

class TaskExecutorReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra("COMMAND") ?: return

        // Execute command in background
        // This would integrate with ShellExecutor
        // For now, just log it
        android.util.Log.d("TaskExecutor", "Executing scheduled task: $command")
    }
}
