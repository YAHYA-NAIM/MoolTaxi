package com.example.mooltaxi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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

        private lateinit var locationButton: ImageButton
        private lateinit var playPauseButton: Button

        private var isPlaying = false

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_map, container, false)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            locationButton = view.findViewById(R.id.locationButton)
            locationButton.setOnClickListener {
                getCurrentLocation()
            }
            setupLocationRequest()
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
                                MarkerOptions().position(currentLatLng).title("Current Location")
                            )
                        } else {
                            currentLocationMarker?.position = currentLatLng
                        }
                    }
                }
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }

        private fun updateLocation(location: Location) {
            // Update the location logic, but for ride calculation logic, move it to the Compteur fragment
        }

    }
