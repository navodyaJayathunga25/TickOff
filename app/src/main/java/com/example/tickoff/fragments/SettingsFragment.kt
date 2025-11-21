package com.example.tickoff.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Switch
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.*
import com.example.tickoff.R
import com.example.tickoff.databinding.FragmentSettingsBinding
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val CHANNEL_ID = "hydration_reminder_channel"
    private val REQUEST_NOTIFICATION_PERMISSION = 1001

    //UI setup
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        createNotificationChannel()

        //Components
        val switchHydration: Switch = binding.switchHydration
        val spinnerInterval: Spinner = binding.spinnerInterval

        spinnerInterval.setSelection(1) // default 1 hour

        // Hydration switch listener
        switchHydration.setOnCheckedChangeListener @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) { _, isChecked ->
            if (isChecked) {
                if (checkNotificationPermission()) {
                    cancelHydrationReminder() // cancel any existing work
                    sendImmediateHydrationNotification() // only one immediate notification
                    scheduleHydrationReminder(spinnerInterval.selectedItem.toString())
                } else {
                    requestNotificationPermission()
                }
            } else {
                cancelHydrationReminder()
            }
        }

        // Spinner interval listener
        spinnerInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (switchHydration.isChecked && checkNotificationPermission()) {
                    cancelHydrationReminder() // cancel old work
                    scheduleHydrationReminder(spinnerInterval.selectedItem.toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Schedule periodic hydration notifications
    private fun scheduleHydrationReminder(intervalText: String) {
        val intervalMinutes = when (intervalText) {
            "30 min" -> 30L
            "1 hour" -> 60L
            "2 hours" -> 120L
            "3 hours" -> 180L
            else -> 60L
        }

        val workRequest = PeriodicWorkRequestBuilder<HydrationWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setInitialDelay(intervalMinutes, TimeUnit.MINUTES) // first run after interval
            .build()

        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    // Cancel scheduled hydration notifications
    private fun cancelHydrationReminder() {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("hydration_reminder")
    }

    // Send a single immediate notification
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendImmediateHydrationNotification() {
        if (!checkNotificationPermission()) return

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Hydration Reminder")
            .setContentText("Time to drink water 💧")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(requireContext())
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Create notification channel (for Android O+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminder"
            val descriptionText = "Reminds you to drink water"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Check runtime permission for notifications (Android 13+)
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // Request notification permission at runtime
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (binding.switchHydration.isChecked) sendImmediateHydrationNotification()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Worker class to send periodic hydration notifications
class HydrationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val CHANNEL_ID = "hydration_reminder_channel"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification() {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Hydration Reminder")
            .setContentText("Time to drink water 💧")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
