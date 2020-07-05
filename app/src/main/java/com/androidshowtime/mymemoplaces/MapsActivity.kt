package com.androidshowtime.mymemoplaces

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    //for static components
    companion object {
        //request code for us on onRequestPermissionsResult
        private const val PERMISSION_REQUEST_CODE = 1

    }

    //variable to monitor permission status
    private var locationPermissionGranted = false

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback

    //variable to store the latest location received
    private lateinit var newLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


        //request location permission with a dialogue
        getLocationPermission()

        //initializing the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //setting up locationRequest parameters
        locationRequest = LocationRequest
                .create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) //Set the desired interval for active location updates
                .setFastestInterval(30000) //Explicitly set the fastest interval for updates


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    fun requestForLocationUpdates() {
        //permission check suppressed as this is already requested on the onCreate()
        if (locationPermissionGranted) {

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallBack,
                Looper.getMainLooper()
                                                              )
        }

    }

    fun centerMapOnLocation(latLng: LatLng, title: String) {


        map.addMarker(
            MarkerOptions()
                    .position(latLng)
                    .title(title)
                     )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        //make set MapsActivity to be a listener for long clicks
        map.setOnMapLongClickListener(this)

        //get intent from getIntent method
        val intent = intent


        //get value from the intent
        val placeNumber = intent.getIntExtra("placeNumber", 0)

        //Zoom in on user location
        locationCallBack = object : LocationCallback() {

            //Called when device location information is available.
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                //null check
                locationResult?.let {


                    //if it is the first item on the list that is called
                    if (placeNumber == 0) {
                        newLocation = it.lastLocation
                        centerMapOnLocation(
                            LatLng(newLocation.latitude, newLocation.longitude), "Your Location"
                                           )


                    }

                    //if other places on the list are clicked
                    else {
                        //minus one because the places list contains one additional info thus
                        // avoiding OutOfBounds exception error
                        val clickedLocation = MainActivity.locations[placeNumber - 1]
                        centerMapOnLocation(clickedLocation, MainActivity.places[placeNumber])


                    }
                }
            }

        }
        requestForLocationUpdates()


    }


    override fun onMapLongClick(latLng: LatLng?) {
        map.clear()
        Timber.i("onMapLongClick () reached")
        latLng?.let {
            val address = geoCodingMethod(it)
            map.addMarker(
                MarkerOptions()
                        .position(it)
                        .title(address)
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                         )
            MainActivity.places.add(address)
            MainActivity.locations.add(it)
            MainActivity.adapter.notifyDataSetChanged()

            MainActivity.saveLists()

            Toast
                    .makeText(this, "Location Saved", Toast.LENGTH_SHORT)
                    .show()
        }

    }

    //geoCodingMethod() extracted here and called inside onMapLongClick() instead of congesting it
    private fun geoCodingMethod(latLng: LatLng): String {
        Timber.i("onMapLongClick () reached")
        val geoCoder = Geocoder(this, Locale.getDefault())
        var address = ""
        try {
            val listOfAddresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)


            //checking size of the list to ensure it has at least 1 item
            if (listOfAddresses.size > 0) {
                //null check on listOfAddresses
                listOfAddresses?.let {

                    if (it[0].thoroughfare != null) {
                        if (it[0].subThoroughfare != null) {

                            address += listOfAddresses[0].subThoroughfare + " "
                        }

                        address += listOfAddresses[0].thoroughfare
                    }
                }
            }


        }
        catch (e: Exception) {
            e.printStackTrace()
        }


        if (address.isEmpty()) {

            address += formatDate()
        }
        return address
    }

    //formatDate() extracted to avoid congestion
    private fun formatDate(): String {
        val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date())

    }

    /*Request location permission, so that we can get the location of the
      device. The result of the permission request is handled by a callback,
      onRequestPermissionsResult */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
                                           ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true

                    //Explain to the user that the feature is unavailable
                }
                else {

                    Toast
                            .makeText(
                                this, "App won't work with this permission", Toast.LENGTH_LONG
                                     )
                            .show()
                }

                return
            }
        }

    }

    //method for requesting permission inside the onCreate()
    private fun getLocationPermission() {
        //Determine whether you have been granted a particular permission.
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
                                             ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                locationPermissionGranted = true
            }
            //check if the system recommends a rationale dialog before asking permission
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast
                        .makeText(this, "Location Permission Needed", Toast.LENGTH_SHORT)
                        .show()
            }

            // Show the permission request dialog
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE

                                                 )
            }
        }
    }


}