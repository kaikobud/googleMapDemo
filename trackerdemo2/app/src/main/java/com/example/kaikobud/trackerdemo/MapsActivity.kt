package com.example.kaikobud.trackerdemo

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.format.Time
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    override fun onMarkerClick(p0: Marker?) = false


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var distanceInMeters:Float = 0.0f
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false



    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3
        private const val REQUEST_CHECK_SETTINGS = 2
    }
    val loc1 = Location("")
    val loc2 = Location("")

    val list: MutableList<LatLng> = ArrayList()
    val timelist: MutableList<String> = ArrayList()
    lateinit var button:Button







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                timelist.add(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date()))

                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        button = findViewById<Button>(R.id.button)
        var durationvalue = findViewById<TextView>(R.id.duration_value)
        //val currentTime:Date = getCurrentTime()
        //timelist.add(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date()))
        //Log.i("aaaaaaaaaaaaa", timelist.toString())
        button.setOnClickListener {
            calculateduration(list,timelist)
        }
        createLocationRequest()
        button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            onPause()
        }



    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled=true
        map.setOnMarkerClickListener(this)
        setUpMap()


    }
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        // 1
        map.isMyLocationEnabled = true

// 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }

    }
    private fun placeMarkerOnMap(location: LatLng) {

        //val markerOptions = MarkerOptions().position(location)

        list.add(location)
        timelist.add(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date()))
        //Log.i("aaaaaa",list.toString())
        //.i("bbbbb",timelist.toString())
        //val titleStr = getAddress(location)  // add these two lines
        //markerOptions.title(titleStr)
        //Log.i("aaaaaaaa",titleStr)

        //map.addMarker(markerOptions)
        drawpolylines(list)
        calculateduration(list,timelist)


    }


    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        locationRequest.interval = 10000

        // 2
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    private fun drawpolylines(list: MutableList<LatLng> = ArrayList()){
        //Log.i("aaaaaaaaaaaaaaaaaa",list.toString())
        for (i in 0 until list.size - 1) {
            val src = list[i]
            val dest = list[i+1]

            // mMap is the Map Object
            val line = map.addPolyline(
                    PolylineOptions().add(
                            LatLng(src.latitude, src.longitude),
                            LatLng(dest.latitude, dest.longitude)
                    ).width(2f).color(Color.BLUE).geodesic(true)
            )

            loc1.latitude = src.latitude
            loc1.longitude = src.longitude


            loc2.latitude = dest.latitude
            loc2.longitude = dest.longitude

            distanceInMeters += loc1.distanceTo(loc2)
            var textview = findViewById<TextView>(R.id.distance_value)
            textview.text = distanceInMeters.toString()
            //Toast.makeText(this,currenttime(),Toast.LENGTH_SHORT).show()
            //Log.i("tttttttttttttttttt",currenttime())
        }


    }

    fun calculateduration(list: MutableList<LatLng>,timelist: MutableList<String>) {

        for (i in 0 until timelist.size - 1 step 5) {
            var simpleDateFormat = SimpleDateFormat("HH:mm a")
            var date1 = simpleDateFormat.parse(timelist[i])
            var date2 = simpleDateFormat.parse(timelist[i+1])

            var difference = date2.getTime() - date1.getTime()
            var days = (difference / (1000*60*60*24))
            var hours = ((difference - (1000*60*60*24*days)) / (1000*60*60))
            var min = ((difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60))

            Log.i("======= Hours"," :: "+hours)
            if(hours>0 || min >=5){
                //Log.i("area",list[i].toString())
                //Log.i("duration",hours.toString()+":"+min.toString())
                //durationvalue

            }

        }
        }

    }



