package com.example.hydrationreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Assuming MainActivity contains the logic to start reminders
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necessary as you're starting an Activity from a context outside of an Activity
                putExtra("restartReminders", true) // Optional: if you handle this in MainActivity to conditionally start reminders
            }
            context.startActivity(mainIntent)
        }
    }
}
