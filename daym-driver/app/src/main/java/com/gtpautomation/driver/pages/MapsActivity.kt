package com.gtpautomation.driver.pages

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.gtpautomation.driver.R
import com.gtpautomation.driver.api_services.ApiCall
import com.gtpautomation.driver.data_models.AppointmentDetails
import com.gtpautomation.driver.utils.DirectionsJSONParser
import org.json.JSONObject
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader


class MapsActivity : AppCompatActivity() {
    private var nearByMap: MapView? = null
    private var googleMap: GoogleMap? = null
    private lateinit var appointmentDetails: AppointmentDetails
    private val wktCenterPoints: MutableList<LatLng> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initViews(savedInstanceState)
        getIntentData()
    }

    private fun getIntentData() {
        appointmentDetails = Gson().fromJson<AppointmentDetails>(
                intent.getStringExtra("appointment"),
                AppointmentDetails::class.java
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.directions_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.google_map_menu -> {
                val reader = WKTReader()


                var uri = "http://maps.google.com/maps?"
                uri += "daddr=${wktCenterPoints.last().longitude},${wktCenterPoints.last().latitude}"

                if (wktCenterPoints.size > 2) {
                    for (p in wktCenterPoints.subList(0, wktCenterPoints.size - 1)) {
                        uri += "+to:${p.longitude},${p.latitude}"
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                try {
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    try {
                        val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        startActivity(unrestrictedIntent)
                    } catch (innerEx: ActivityNotFoundException) {
                        Toast.makeText(
                            this,
                            getString(R.string.install_map_application),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews(savedInstanceState: Bundle?) {
        supportActionBar?.title = "Direction"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nearByMap = findViewById(R.id.map_page_map)
        nearByMap?.onCreate(savedInstanceState)
        nearByMap?.getMapAsync { it ->
            googleMap = it
            setUpMap()
        }
    }
    private fun setUpMap() {
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true
        googleMap?.uiSettings?.isZoomGesturesEnabled = true
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false

        for (order in appointmentDetails.orders) {
            val reader = WKTReader()

            try {
                val dockGeometry = reader.read(order.dock.wkt)
                val dockCoordinate = dockGeometry.boundary.centroid

                wktCenterPoints.add(LatLng(dockCoordinate.x, dockCoordinate.y))

                val warehouseGeometry = reader.read(order.warehouse.wkt)
                val warehouseCoordinate = warehouseGeometry.boundary.centroid

                wktCenterPoints.add(LatLng(warehouseCoordinate.x, warehouseCoordinate.y))

                drawGeometryInMap(dockGeometry, order.dock.name)
                drawGeometryInMap(warehouseGeometry, order.warehouse.name)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        if (wktCenterPoints.isEmpty()) return
        googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        wktCenterPoints.first(),
                        20F
                )
        )
        drawRoute(wktCenterPoints)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        }
    }

    private fun drawText(name: String, geometry: Geometry) {
        val textView = TextView(applicationContext)
        textView.text = name
        textView.textSize = 16.0f

        val paintText: Paint = textView.paint

        val boundsText = Rect()
        paintText.getTextBounds(name, 0, textView.length(), boundsText)
        paintText.textAlign = Paint.Align.CENTER

        val conf: Bitmap.Config = Bitmap.Config.ARGB_8888
        val bmpText: Bitmap = Bitmap.createBitmap(boundsText.width() + 2
                * 12, boundsText.height() + 2 * 12, conf)

        val canvasText = Canvas(bmpText)
        paintText.color = Color.RED

        canvasText.drawText(name, canvasText.width / 2.0f,
                canvasText.height - 12.0f - boundsText.bottom, paintText)

        val markerOptions = MarkerOptions()
                .position(LatLng(geometry.centroid.x, geometry.centroid.y))
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1f)

        googleMap?.addMarker(markerOptions)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Allow location permission to continue", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    googleMap?.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(applicationContext, "Allow location permission to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun drawGeometryInMap(geometry: Geometry, name: String) {
        val points: MutableList<LatLng> = mutableListOf()
        try {
            for (coordinate in geometry.boundary.coordinates) {
                points.add(LatLng(coordinate.x, coordinate.y))
            }
            drawText(name, geometry)
            if (geometry.geometryType == "Polygon") {
                googleMap?.addPolygon(PolygonOptions().addAll(points).strokeWidth(5.0f).fillColor(Color.argb(100, 25, 118, 210)))
            } else if (geometry.geometryType == "Circle") {
                googleMap?.addCircle(CircleOptions().center(LatLng(geometry.centroid.x, geometry.centroid.y)).radius(geometry.length).strokeWidth(5.0f).fillColor(Color.argb(100, 25, 118, 210)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        nearByMap?.onPause()
    }

    override fun onStart() {
        super.onStart()
        nearByMap?.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        nearByMap?.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        nearByMap?.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        nearByMap?.onStop()
    }

    override fun onResume() {
        super.onResume()
        nearByMap?.onResume()
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        when (AppCompatDelegate.getDefaultNightMode()) {
//            AppCompatDelegate.MODE_NIGHT_YES -> {
//                googleMap?.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                        this@MapsActivity,
//                        R.raw.map_style
//                    )
//                )
//            }
//            AppCompatDelegate.MODE_NIGHT_NO -> {
//                googleMap?.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                        this@MapsActivity,
//                        R.raw.map_style
//                    )
//                )
//            }
//        }
//    }

    private fun drawRoute(points: MutableList<LatLng>) {
        val locationManager: LocationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    var url: String = "https://maps.googleapis.com/maps/api/directions/json"
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            10F
                        )
                    )
                    url += "?origin=${location.latitude},${location.longitude}&destination=${points.last().latitude},${points.last().longitude}&key=${
                        resources.getString(
                            R.string.google_maps_key
                        )
                    }"
                    if (points.size > 2) {
                        var wayPoints: String = ""
                        for (point in points.subList(0, points.size - 1)) {
                            wayPoints += "via:${point.latitude}%2C${point.longitude}%7C"
                        }
                        url += "&waypoints=$wayPoints"
                    }
                    Log.i("DIRECTION", url)
                    ApiCall.connect(
                        applicationContext, Request.Method.GET,
                        url,
                        null, null, null,
                        { response ->
                            try {
                                var p: ArrayList<LatLng?>? = null
                                var lineOptions: PolylineOptions? = null
                                val jObject = JSONObject(response)
                                val parser = DirectionsJSONParser()

                                // Starts parsing data
                                val routes: List<List<HashMap<String, String>>> =
                                    parser.parse(jObject)
                                for (i in routes.indices) {
                                    p = ArrayList()
                                    lineOptions = PolylineOptions()

                                    // Fetching i-th route
                                    val path: List<HashMap<String, String>> = routes.get(i)

                                    // Fetching all the points in i-th route
                                    for (point in path) {
                                        val lat = point["lat"]!!.toDouble()
                                        val lng = point["lng"]!!.toDouble()
                                        val position = LatLng(lat, lng)
                                        p.add(position)
                                    }

                                    // Adding all the points in the route to LineOptions
                                    lineOptions.addAll(p)
                                    lineOptions.width(8.0f)
                                    lineOptions.color(Color.RED)
                                }
                                // Drawing polyline in the Google Map for the i-th route
                                if (lineOptions != null) {
                                    googleMap?.addPolyline(lineOptions)

                                } else
                                    Toast.makeText(
                                        applicationContext,
                                        getString(R.string.no_route_found),
                                        Toast.LENGTH_LONG
                                    ).show();
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }, { volleyError ->
                            Toast.makeText(
                                applicationContext,
                                volleyError.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        })

                }
            })
    }
}