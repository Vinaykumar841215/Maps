package com.example.maps

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView

import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mMap: GoogleMap
    private var polyline : PolylineOptions? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val searchView = findViewById<SearchView>(R.id.searchView)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
                    updateLocation(location)
                }
            }
        }
        val mapOptionsBtn: ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(this, mapOptionsBtn)
        popupMenu.menuInflater.inflate(R.menu.menu_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)

            true
        }
        mapOptionsBtn.setOnClickListener {
            popupMenu.show()
        }
    }
    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map_map -> mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val chhapra = LatLng(24.779566, 89.749886)
        mMap.addMarker(MarkerOptions().position(chhapra).title("Current Location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(chhapra))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chhapra, 16f))

        val polylineOptions = PolylineOptions()
            .add(LatLng(25.779566, 84.749886))
            .add(LatLng(25.982473,84.923724 ))
            .color(0xFF0000FF.toInt())
            .width(5f)
        polyline = polylineOptions


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null)
                updateLocation(it)
        }

        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY

        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, null
        )
        mMap.setOnMapClickListener {
            onMapClick(it)
        }

    }
    fun onMapClick(clickedLatLng: LatLng) {
        // Convert LatLng to Location
        val clickedLocation = Location("")
        clickedLocation.latitude = clickedLatLng.latitude
        clickedLocation.longitude = clickedLatLng.longitude

        // Show location dialog for the clicked location
        showLocationDialog(clickedLocation)

    }
    private fun showLocationDialog(location: Location){
        val geocoder=Geocoder(this, Locale.getDefault())
        val addresses=geocoder.getFromLocation(location.latitude,location.longitude,1)
        val address = if (addresses?.isNotEmpty()==true){
            addresses[0]?.getAddressLine(0)
        }
        else{
            "Address not available"
        }
        AlertDialog.Builder(this)
            .setTitle("Location Information")
            .setMessage("Latitude:${location.latitude}\n Longitude:${location.latitude}\n Address: $address")
            .setPositiveButton("ok",DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .create()
            .show()

    }
    private fun updateLocation (location: Location){
        val currentLocation = LatLng(location.latitude,location.longitude)
        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))

    }
}