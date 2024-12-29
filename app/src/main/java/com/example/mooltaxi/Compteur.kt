package com.example.mooltaxi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class Compteur : Fragment() {

    lateinit var playPauseButton: ImageButton
    private lateinit var totalFareTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView

    private var isPlaying = false
    private var totalFare: Double = 0.0
    private var distanceTraveled: Double = 0.0
    private var startTime: Long = 0
    private var elapsedTimeInMillis: Long = 0
    private var fareUpdateHandler = Handler()

    private val baseFare: Double = 2.5
    private val farePerKm: Double = 1.5
    private val farePerMinute: Double = 0.5

    private val NOTIFICATION_CHANNEL_ID = "ride_channel"
    private val NOTIFICATION_ID = 1

    private val REQUEST_CODE_POST_NOTIFICATIONS = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compteur, container, false)

        playPauseButton = view.findViewById(R.id.playPauseButton)
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        totalFareTextView = view.findViewById(R.id.totalFare)
        distanceTextView = view.findViewById(R.id.distance)
        timeTextView = view.findViewById(R.id.time)

        return view
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            // Stop the ride
            isPlaying = false
            playPauseButton.setImageResource(R.drawable.play) // Change icon to play
            stopRide()
        } else {
            // Start the ride
            isPlaying = true
            totalFare = 0.0
            totalFareTextView.text = String.format("%.2f", totalFare) // Reset the fare to 0
            playPauseButton.setImageResource(R.drawable.pause) // Change icon to pause
            startRide()
        }
    }

    private fun startRide() {
        startTime = System.currentTimeMillis() - elapsedTimeInMillis // Adjust for already elapsed time
        startTimer()
    }

    private fun stopRide() {
        stopTimer()
        calculateFare()
        playPauseButton.visibility = View.VISIBLE  // Ensure the button is visible after stopping
        playPauseButton.setImageResource(R.drawable.play)  // Set icon to "play" after stopping

        // Show the notification after stopping the ride
        showRideDetailsNotification()
    }

    private fun startTimer() {
        fareUpdateHandler.postDelayed(object : Runnable {
            override fun run() {
                // Update time and fare every second
                val elapsedMillis = System.currentTimeMillis() - startTime
                elapsedTimeInMillis = elapsedMillis
                val hours = (elapsedMillis / 3600000).toInt()
                val minutes = (elapsedMillis % 3600000 / 60000).toInt()
                val seconds = (elapsedMillis % 60000 / 1000).toInt()

                // Format time as HH:mm:ss
                val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                timeTextView.text = formattedTime

                // Simulate the distance only if you want, but no fake increment now
                // distanceTraveled += 0.1 // Do not increase distance artificially here

                // Calculate the total fare based on time
                calculateFare()

                // Update the UI every second
                fareUpdateHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun stopTimer() {
        fareUpdateHandler.removeCallbacksAndMessages(null) // Stop the fare update handler
    }

    private fun calculateFare() {
        val elapsedTimeInMinutes = elapsedTimeInMillis / 60000.0
        totalFare = baseFare + (distanceTraveled * farePerKm) + (elapsedTimeInMinutes * farePerMinute)
        totalFareTextView.text = String.format("%.2f", totalFare) // Display fare as numeric value
        distanceTextView.text = String.format("%.2f", distanceTraveled) // Display distance as numeric value
    }

    private fun showRideDetailsNotification() {
        // Check for notification permission
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            // Request notification permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        } else {
            // Permission granted, proceed with notification
            createNotificationChannel() // Create notification channel if needed

            val elapsedTimeInMinutes = elapsedTimeInMillis / 60000.0
            totalFare = baseFare + (distanceTraveled * farePerKm) + (elapsedTimeInMinutes * farePerMinute)

            val notificationText = """
                ${String.format("%.2f", elapsedTimeInMinutes)} minutes
                ${String.format("%.2f", distanceTraveled)} km
                Fare: ${String.format("%.2f", totalFare)} DH
            """.trimIndent()

            val builder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.ride_ended))
                .setContentText(getString(R.string.ride_details))
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(requireContext())) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Ride Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ride details notifications"
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showRideDetailsNotification()
            } else {

            }
        }
    }
}
