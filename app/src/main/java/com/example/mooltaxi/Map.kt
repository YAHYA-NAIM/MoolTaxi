package com.example.mooltaxi

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class Map: Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocationMarker: Marker? = null

    private lateinit var playPauseButton: Button
    private var isPlaying = false

    // Fare calculation variables
    private val baseFare: Double = 2.5
    private val farePerKm: Double = 1.5
    private val farePerMinute: Double = 0.5

    private var totalFare: Double = 0.0
    private var startLocation: Location? = null
    private var previousLocation: Location? = null
    private var elapsedTimeInMinutes: Double = 0.0
    private var startTime: Long = 0
    private var distanceTraveled: Double = 0.0
    private var handler = Handler()
    private var runnable: Runnable? = null

    private lateinit var distanceTimeInfoTextView: TextView
    private lateinit var totalFareTextView: TextView

    private val NOTIFICATION_CHANNEL_ID = "ride_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationButton: ImageButton = view.findViewById(R.id.locationButton)
        locationButton.setOnClickListener {
            getCurrentLocation()
        }

        playPauseButton = view.findViewById(R.id.playPauseButton)
        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        setupLocationRequest()

        distanceTimeInfoTextView = view.findViewById(R.id.distanceTimeInfo)
        totalFareTextView = view.findViewById(R.id.totalFare)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    if (currentLocationMarker == null) {
                        currentLocationMarker = mMap.addMarker(
                            MarkerOptions().position(currentLatLng).title(getString(R.string.current_location))
                        )
                    } else {
                        currentLocationMarker?.position = currentLatLng
                    }

                    if (isPlaying) {
                        startLocation = startLocation ?: it
                    }
                } ?: Toast.makeText(requireContext(), getString(R.string.unable_to_get_location), Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            isPlaying = false
            playPauseButton.text = getString(R.string.play)
            stopRide()
            totalFareTextView.text = getString(R.string.total_fare, totalFare)

            // Change background tint to Rouge (red) when paused
            playPauseButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.Vert)
        } else {
            totalFare = 0.0
            totalFareTextView.text = getString(R.string.total_fare, totalFare)
            isPlaying = true
            playPauseButton.text = getString(R.string.pause)
            startRide()

            // Change background tint to Vert (green) when playing
            playPauseButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.Rouge)
        }
    }


    private fun startRide() {
        startTime = System.currentTimeMillis()
        startTimer()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, requireActivity().mainLooper)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        Toast.makeText(requireContext(), getString(R.string.ride_started), Toast.LENGTH_SHORT).show()
    }

    private fun stopRide() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopTimer()
        calculateFare()
    }

    private fun updateLocation(location: Location) {
        if (previousLocation != null) {
            distanceTraveled += previousLocation!!.distanceTo(location) / 1000.0
        }
        previousLocation = location
    }

    private fun calculateFare() {
        elapsedTimeInMinutes = (System.currentTimeMillis() - startTime) / 60000.0
        totalFare = baseFare + (distanceTraveled * farePerKm) + (elapsedTimeInMinutes * farePerMinute)
        Toast.makeText(requireContext(), getString(R.string.total_fare, totalFare), Toast.LENGTH_LONG).show()
    }

    private fun startTimer() {
        if (runnable == null) {
            runnable = object : Runnable {
                override fun run() {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val elapsedMinutes = elapsedMillis / 60000
                    val elapsedSeconds = (elapsedMillis % 60000) / 1000
                    val formattedTime = String.format(getString(R.string.time), elapsedMinutes, elapsedSeconds)
                    val updatedText = String.format(getString(R.string.distance_km), distanceTraveled) + "\n\n" + formattedTime
                    distanceTimeInfoTextView.text = updatedText
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.post(runnable!!)
    }

    private fun stopTimer() {
        runnable?.let {
            handler.removeCallbacks(it)
        }
        runnable = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ride Notifications"
            val descriptionText = "Notifications for ride status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showRideDetailsNotification() {
        elapsedTimeInMinutes = (System.currentTimeMillis() - startTime) / 60000.0
        totalFare = baseFare + (distanceTraveled * farePerKm) + (elapsedTimeInMinutes * farePerMinute)

        val notificationText = """
        ${getString(R.string.time)}: ${String.format("%.2f", elapsedTimeInMinutes)} minutes
        ${getString(R.string.distance_km)}: ${String.format("%.2f", distanceTraveled)} km
        Fare: ${String.format("%.2f", totalFare)} DH
        """.trimIndent()

        val builder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.ride_ended))
            .setContentText(getString(R.string.ride_details))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
