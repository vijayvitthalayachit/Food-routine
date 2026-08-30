package com.foodroutine.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.foodroutine.app.notifications.DailyReminderWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class FoodRoutineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDailyReminder()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DailyReminderWorker.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Fire the grocery/prep reminder once a day around 18:00 local time. */
    private fun scheduleDailyReminder() {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(18, 0))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val initialDelay = Duration.between(now, next)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(Duration.ofDays(1))
            .setInitialDelay(initialDelay)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
