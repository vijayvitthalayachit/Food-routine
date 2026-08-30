package com.foodroutine.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foodroutine.app.R
import com.foodroutine.app.data.FoodRoutineRepository
import com.foodroutine.core.grocery.GroceryScheduler
import com.foodroutine.core.grocery.TaskType
import java.time.LocalDate

/**
 * Evening reminder: what to soak/ferment tonight and what to buy for the
 * coming days (regular items one day ahead, soaked two, sprouted three).
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = FoodRoutineRepository(applicationContext)
        val plan = repository.loadActivePlan() ?: return Result.success()

        val today = LocalDate.now()
        val tasks = GroceryScheduler.schedule(plan).filter { it.date == today }
        if (tasks.isEmpty()) return Result.success()

        val buys = tasks.filter { it.type == TaskType.BUY }
        val preps = tasks.filter { it.type != TaskType.BUY }

        val lines = buildList {
            if (buys.isNotEmpty()) {
                add("Buy: " + buys.joinToString { "${it.ingredientName} (${it.quantityGrams.toInt()} g)" })
            }
            for (p in preps) {
                val verb = if (p.type == TaskType.SOAK) "Soak tonight" else "Set to sprout/ferment tonight"
                add("$verb: ${p.ingredientName} for ${p.recipeName} (${p.cookDate})")
            }
        }

        postNotification(lines)
        return Result.success()
    }

    private fun postNotification(lines: List<String>) {
        val granted = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted && android.os.Build.VERSION.SDK_INT >= 33) return

        val style = NotificationCompat.InboxStyle()
        lines.forEach { style.addLine(it) }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Food Routine: groceries & prep")
            .setContentText(lines.firstOrNull() ?: "")
            .setStyle(style)
            .setAutoCancel(true)
            .build()

        applicationContext.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "daily_reminders"
        const val WORK_NAME = "food_routine_daily_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
