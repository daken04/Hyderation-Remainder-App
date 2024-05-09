package com.example.hydrationreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, "hydrationReminder")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Time to Hydrate")
            .setContentText("Have a glass of water to stay hydrated!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(200, notification)
        }
    }
}
