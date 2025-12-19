package com.example.practica3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var buttonProducts: Button
    private val LOCATION_PERMISSION_REQUEST = 1
    private var locationOverlay: MyLocationNewOverlay? = null
    private val fallbackPoint = GeoPoint(37.19715325305848, -3.6244863308941815)

    private val almacenes = listOf(
        GeoPoint(37.187883, -3.624462),
        GeoPoint(37.164199, -3.609143),
        GeoPoint(37.156979, -3.606891)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración global de OSMDroid antes de setContentView
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = "com.example.practica3"

        setContentView(R.layout.activity_map)

        // Configura el MapView
        mapView = findViewById(R.id.map)

        mapView.setMultiTouchControls(true)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        buttonProducts = findViewById(R.id.buttonIrProdcuts)
        buttonProducts.setOnClickListener { finish() }

        if (hasLocationPermissions()) {
            initializeMap()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationOverlay?.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        locationOverlay?.disableMyLocation()
        mapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (hasLocationPermissions()) {
                initializeMap()
            } else {
                // Si el usuario no da permisos, centramos en fallback para que el mapa se vea
                centerOnFallback()
                addStoreMarkers()
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }


    private fun initializeMap() {
        // Zoom inicial (luego lo ajustamos cuando llegue la ubicación)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(fallbackPoint)

        // Overlay de ubicación (GPS)
        val overlay = MyLocationNewOverlay(
            GpsMyLocationProvider(this),
            mapView
        )
        locationOverlay = overlay

        overlay.enableMyLocation()
        mapView.overlays.add(overlay)

        // Marcadores
        addStoreMarkers()

        // Si en 6 segundos no hay fix válida, dejamos el fallback (evita "perderse")
        Handler(Looper.getMainLooper()).postDelayed({
            val loc = overlay.myLocation
            if (!isValidGeoPoint(loc)) {
                centerOnFallback()
            }
        }, 6000)

        // Cuando llegue la primera fix, centramos solo si es válida y activamos follow
        overlay.runOnFirstFix {
            runOnUiThread {
                val loc = overlay.myLocation
                if (isValidGeoPoint(loc)) {
                    mapView.controller.setZoom(17.0)
                    mapView.controller.setCenter(loc)
                    overlay.enableFollowLocation() // activarlo DESPUÉS evita saltos raros
                } else {
                    centerOnFallback()
                }
            }
        }

        mapView.invalidate()
    }

    private fun isValidGeoPoint(p: GeoPoint?): Boolean {
        if (p == null) return false
        // Evita (0,0) y evita valores imposibles
        val lat = p.latitude
        val lon = p.longitude

        if (lat == 0.0 && lon == 0.0) return false
        if (lat !in -90.0..90.0) return false
        if (lon !in -180.0..180.0) return false

        // (Opcional) evita ubicaciones con muy poca precisión si tuvieras acceso al Location real.
        return true
    }

    private fun centerOnFallback() {
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(fallbackPoint)
        mapView.invalidate()
    }

    private fun addStoreMarkers() {
        almacenes.forEach { location ->
            val marker = Marker(mapView)
            marker.position = location
            marker.title = "Almacén"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }
    }
}