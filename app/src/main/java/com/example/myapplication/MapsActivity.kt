package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.models.MyLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.util.*
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import kotlin.math.*

const val RADIUS_OF_EARTH_KM = 6371

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var distanceCovered = 0.0
    private var cameraPosition: CameraPosition? = null
    private lateinit var mGeocoder : Geocoder
    private lateinit var binding: ActivityMapsBinding
    private var localizaciones = mutableMapOf<String, JSONObject>()
    private var previousLocation = LatLng(-33.8523341, 151.2106085)
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var lightSensorListener: SensorEventListener
    
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    
    
    private var lastKnownLocation: Location? = null
    

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocationRequest = createLocationRequest()
        mGeocoder = Geocoder(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 5000) {
                        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.dark_json))
                    } else {
                        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.light_json))
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        
        
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        binding.texto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = binding.texto.text.toString()
                if (!addressString.isEmpty()) {
                    try {
                        val addresses = mGeocoder.getFromLocationName(addressString, 2)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)
                            if (mMap != null) {
                                mFusedLocationClient.lastLocation
                                    .addOnSuccessListener(this, OnSuccessListener { location ->
                                        if (location != null) {
                                            Toast.makeText(this, "La distancia entre los marcadores es ${distance(location.latitude, location.longitude, position.latitude, position.longitude)}", Toast.LENGTH_SHORT).show()
                                            
                                        }
                                    })
                                mMap?.addMarker(MarkerOptions().position(position).title(mGeocoder.getFromLocation(position.latitude, position.longitude, 1)!![0].getAddressLine(0)))
                                mMap?.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(position, DEFAULT_ZOOM.toFloat()))
                                return@setOnEditorActionListener true
                            } else {
                                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                            }
                            return@setOnEditorActionListener false
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return@setOnEditorActionListener false
                    }
                } else {
                    Toast.makeText(this, "La dirección esta vacía", Toast.LENGTH_SHORT).show()
                }
            }
            return@setOnEditorActionListener false
        }
    
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    updateDistanceCovered(location)
                }
                if (location != null && distanceCovered > 30) {
                    distanceCovered = 0.0
                    writeJSONObject(location)
                    updateMarkerAndCamera(location)
                }
            }
        }
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        
    }
    
    private fun updateMarkerAndCamera(location: Location) {
        val newPosition = LatLng(location.latitude, location.longitude)
        val address = mGeocoder.getFromLocation(newPosition.latitude, newPosition.longitude, 1)?.get(0)
            ?.getAddressLine(0)
        mMap?.clear()
        mMap?.addMarker(MarkerOptions().position(newPosition).title(address))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, DEFAULT_ZOOM.toFloat()))
    }
    
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(lightSensorListener, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }
    
    
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        mMap?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }
    
    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 100000L // Adjust the interval when distance covered exceeds the threshold
        
        return locationRequest
    }
    
    private fun updateDistanceCovered(currentLocation: Location) {
        val res = distance(previousLocation.latitude, previousLocation.longitude, currentLocation.latitude, currentLocation.longitude)
        distanceCovered = res
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.mMap = map
        
        getLocationPermission()

        startLocationUpdates()

        updateLocationUI()
    
        getDeviceLocation()
    
        mMap?.setOnMapLongClickListener { latLng ->
            mMap!!.clear()
            mMap!!.addMarker(MarkerOptions().position(latLng).title(mGeocoder.getFromLocation(latLng.latitude, latLng.longitude,  1)
                !![0].getAddressLine(0)))
            mFusedLocationClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener { location ->
                    if (location != null) {
                        Toast.makeText(this, "La distancia entre los marcadores es ${distance(location.latitude, location.longitude, latLng.latitude, latLng.longitude)}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    
    
    }
    
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = mFusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    
    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        
    }
    
    private fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lngDistance / 2) * sin(lngDistance / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }
    
    private fun writeJSONObject(location: Location) {
        val currentTimeMillis = System.currentTimeMillis()
        localizaciones[currentTimeMillis.toString()] = MyLocation(
            Date(currentTimeMillis), location.latitude, location.longitude
        ).toJSON()
        
        val filename = "locations.json"
        val jsonArray = JSONArray(localizaciones.values)
        val jsonString = jsonArray.toString()
        
        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            val output = BufferedWriter(FileWriter(file))
            output.write(jsonString)
            output.close()
            Toast.makeText(applicationContext, "Location saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.d(null, "Algo salió mal")
        }
    }
}


