package com.moulton.suunav

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
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
        map.imageManager = RegionManager(
            BitmapRegionDecoder.newInstance(
                resources.openRawResource( + R.drawable.suu),
                true
            )
        ).apply {
            setOnBufferChange { map.postInvalidate() }
        }
        map.route = map.place.getRoute(place.graph.points[0],place.graph.points[5])

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            val locationRequest = LocationRequest.create()?.apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val c = this.baseContext
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)
                    p0 ?: return
                    place.curLocation = p0.lastLocation
                    map.route = map.place.getRoute(
                        map.place.graph.points.find{p -> p.Id == 1}!!,
                        map.place.graph.points.find{p -> p.Id == 5}!!
                        )
                    map.invalidate()
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