package com.moulton.suunav

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var place : Place
    lateinit var fusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        place = PlaceParser(this).parse(R.xml.points,R.xml.paths)
        map.place = place


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            val locationRequest = LocationRequest.create()?.apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)
                    p0 ?: return
                    place.cur_location = p0.lastLocation
                }
            }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
        } else {
            //TODO ask for permision
            Toast.makeText(this, "No permission for location", Toast.LENGTH_SHORT).show()
        }


    }

}