package com.example.myapplication

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import android.os.Build
import androidx.annotation.RequiresApi
import android.content.Intent
import android.location.LocationManager

class MainActivity : AppCompatActivity() {
	
	@RequiresApi(Build.VERSION_CODES.S)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		val imageButton1 = findViewById<ImageButton>(R.id.imageButton1)
		val imageButton2 = findViewById<ImageButton>(R.id.imageButton2)
		val imageButton3 = findViewById<ImageButton>(R.id.imageButton3)
		
		imageButton1.setOnClickListener {
			if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				openActivity(ContactsActivity())
			} else {
				requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
			}
		}
		
		imageButton2.setOnClickListener {
			if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
				openActivity(UploadActivity())
			} else {
				requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 42)
			}
		}
		
		imageButton3.setOnClickListener {
			if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				openActivity(MapsActivity())
			} else {
				requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 11)
			}
		}
	}
	
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			openActivity(ContactsActivity())
		} else if(requestCode == 11 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
			openActivity(MapsActivity())
		} else if(requestCode == 42 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			openActivity(UploadActivity())
		}
		else {
			Snackbar.make(
				findViewById(android.R.id.content),
				"Acceso denegado",
				Snackbar.LENGTH_LONG
			).show()
		}
	}
	
	private fun openActivity(activity: Activity) {
		val intent = Intent(this, activity::class.java)
		startActivity(intent)
	}
	private fun checkLocationServicesEnabled(locationManager: LocationManager): Boolean {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
	}
	
	
}
