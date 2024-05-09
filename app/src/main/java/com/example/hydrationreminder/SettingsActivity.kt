package com.example.hydrationreminder

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val startTimePicker: TimePicker = findViewById(R.id.startTimePicker)
        val endTimePicker: TimePicker = findViewById(R.id.endTimePicker)
        val intervalPicker: NumberPicker = findViewById(R.id.intervalPicker)
        val saveSettingsButton: Button = findViewById(R.id.saveSettingsButton)

        // Initialize NumberPicker for intervals
        intervalPicker.minValue = 1  // Minimum interval of 1 hour
        intervalPicker.maxValue = 12 // Maximum interval of 12 hours

        // Set the current value from SharedPreferences
        val sharedPreferences = getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE)
        intervalPicker.value = sharedPreferences.getInt("interval", 2)  // Default or saved value

        saveSettingsButton.setOnClickListener {
            with(sharedPreferences.edit()) {
                // Save the time and interval settings
                putInt("startHour", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) startTimePicker.hour else startTimePicker.currentHour)
                putInt("startMinute", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) startTimePicker.minute else startTimePicker.currentMinute)
                putInt("endHour", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) endTimePicker.hour else endTimePicker.currentHour)
                putInt("endMinute", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) endTimePicker.minute else endTimePicker.currentMinute)
                putInt("interval", intervalPicker.value)
                putBoolean("settingsChanged", true)  // Flag to indicate that settings have changed
                apply()  // Commit the changes
            }
            finish() // Close the activity
        }
    }
}
