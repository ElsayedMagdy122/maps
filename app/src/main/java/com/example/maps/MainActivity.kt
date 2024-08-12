package com.example.maps


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import okhttp3.internal.wait


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // احصل على SupportMapFragment واشعر بالإشعار عندما تكون الخريطة جاهزة للاستخدام
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapsFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getCurrentLocation()
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style))
    }


    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))

                val sampleLatitude = 37.7749  // إحداثيات سان فرانسيسكو
                val sampleLongitude = -122.4194  // إحداثيات سان فرانسيسكو
                fetchNearbyHospitals(sampleLatitude, sampleLongitude)

                //fetchNearbyHospitals(it.latitude, it.longitude)
            }
        }
    }

    private fun fetchNearbyHospitals(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val radius = 5000 // المسافة بالأمتار
        val type = "restaurant"
        val apiKey = getString(R.string.API_KEY)

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getNearbyMosques(location, radius, type, apiKey)

                if (response.results.isNotEmpty()) {
                    Log.d("mapsData", response.results.toString())
                    for (result in response.results) {
                        val latLng = LatLng(result.geometry.location.lat, result.geometry.location.lng)
                        mMap.addMarker(MarkerOptions().position(latLng).title(result.name))
                    }
                } else {
                    Log.d("fetchNearbyHospitals", "No results found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("fetchNearbyHospitals", "Error: ${e.message}")
            }
        }
    }

    fun mapsSetting() {
        val newzoom = CameraUpdateFactory.newLatLngZoom(LatLng(30.4928, 31.2919), 8F)
        mMap?.animateCamera(newzoom)
        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap?.uiSettings!!.isZoomControlsEnabled = true
        mMap?.uiSettings!!.isCompassEnabled = true
        mMap?.uiSettings!!.isScrollGesturesEnabled = false
        mMap?.uiSettings!!.isZoomGesturesEnabled = false
        mMap?.setPadding(20, 20, 20, 20)
        mMap?.setOnMapClickListener {
            mMap?.clear()
            addMark(it)
        }
    }
    private fun addMark(it: LatLng) {
        val marker2 = MarkerOptions()
        marker2.title(it.longitude.toString() + "" + it.latitude.toString())
        marker2.position(LatLng(it.latitude, it.longitude))
        mMap?.addMarker(marker2)
    }


}
