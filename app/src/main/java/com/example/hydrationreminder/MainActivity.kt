package com.example.hydrationreminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView
import android.widget.NumberPicker
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statusTextView: TextView
    private lateinit var waterIntakePicker: NumberPicker

    private val REQUEST_CODE_POST_NOTIFICATIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
        statusTextView = findViewById(R.id.statusTextView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATIONS)
            }
        }

        createNotificationChannel()
        // Restore reminder status from SharedPreferences
        if (sharedPreferences.getBoolean("ReminderActive", false)) {
            startReminders(null) // Modified to handle nullable view parameter
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Hydration Reminders"
            val channelDescription = "Notifications for hydration reminders"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("hydrationReminder", channelName, channelImportance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if settings were changed and if reminders need to be restarted
        if (sharedPreferences.getBoolean("settingsChanged", false)) {
            sharedPreferences.edit().putBoolean("settingsChanged", false).apply() // Reset the flag
            startReminders(null) // Restart reminders with new settings
        }
    }

    fun startReminders(view: View?) {  // Now accepts nullable View to handle non-UI invocations
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcast::class.java)

        // First cancel any existing reminders to avoid duplicates
        cancelReminders(null)

        val startHour = sharedPreferences.getInt("startHour", 8)
        val startMinute = sharedPreferences.getInt("startMinute", 0)
        val endHour = sharedPreferences.getInt("endHour", 20)
        val interval = sharedPreferences.getInt("interval", 2) * 60 * 60 * 1000 // Interval in milliseconds

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, startMinute)
        calendar.set(Calendar.HOUR_OF_DAY, startHour)

        val endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, endHour)
        endTime.set(Calendar.MINUTE, 0)
        endTime.set(Calendar.SECOND, 0)

        while (calendar.before(endTime)) {
            val pendingIntent = PendingIntent.getBroadcast(
                this, calendar.get(Calendar.HOUR_OF_DAY), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            calendar.add(Calendar.MILLISECOND, interval)
        }
        statusTextView.text = "Reminder is Active"
        saveReminderStatus(true)
    }

    fun cancelReminders(view: View?) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcast::class.java)

        // Attempt to cancel all reminders set from 0 to 23 hours, assuming they could have been set.
        for (hour in 0..23) {
            val pendingIntent = PendingIntent.getBroadcast(
                this, hour, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        statusTextView.text = "Reminders stopped"
        saveReminderStatus(false)
    }

    private fun saveReminderStatus(isActive: Boolean) {
        sharedPreferences.edit().putBoolean("ReminderActive", isActive).apply()
    }

    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startReminders(null) // No view is necessary here
            } else {
                // Permission denied, handle appropriately
            }
        }
    }
}


