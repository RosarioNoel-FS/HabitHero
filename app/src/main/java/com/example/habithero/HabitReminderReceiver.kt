package com.example.habithero

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "Your Habit"

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Show notification
                showNotification(context, habitId, habitName)

                // Fetch the latest habit data to reschedule for the next day
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val habit = FirebaseHelper().getHabitSuspend(userId, habitId)
                    if (habit != null && habit.reminderEnabled) {
                        // Reschedule the alarm for the next day
                        NotificationScheduler(context).schedule(habit)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, habitId: String, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to complete your habits"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch MainActivity with a deep link
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("habithero://habit/$habitId"),
            context,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(), // Unique request code
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // A better icon
            .setContentTitle("Habit Reminder")
            .setContentText("Don't forget to complete: $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the intent
            .setAutoCancel(true) // Notification dismisses on tap
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_HABIT_ID = "EXTRA_HABIT_ID"
        const val EXTRA_HABIT_NAME = "EXTRA_HABIT_NAME"
        private const val CHANNEL_ID = "HABIT_REMINDER_CHANNEL"
    }
}
