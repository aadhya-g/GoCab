//Trackridescreen

package com.example.gocab.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.gocab.SocketHandler
import com.example.gocab.utils.getAddressFromLatLng
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun TrackRideScreen(
    rideId: Int,
    onBack: () -> Unit,
    onHome: () -> Unit,
) {
    BackHandler { onHome() }
    val context = LocalContext.current
    // 🔹 STATES
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var driverMarker by remember { mutableStateOf<Marker?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    var driverLat by remember { mutableStateOf(0.0) }
    var driverLng by remember { mutableStateOf(0.0) }
    var userLat by remember { mutableStateOf(0.0) }
    var userLng by remember { mutableStateOf(0.0) }
    var driverAddress by remember { mutableStateOf("Locating driver...") }
    // 🔹 LOCATION CLIENT
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // 🔹 USER LOCATION FETCH
    LaunchedEffect(Unit) {if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return@LaunchedEffect
    }
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                userLat = it.latitude
                userLng = it.longitude
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // 🔹 OSMDROID CONFIG
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Driver near: $driverAddress",
            modifier = Modifier.padding(16.dp)
        )
        // 🔹 MAP VIEW
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                val map = MapView(ctx)
                map.setMultiTouchControls(true)

                val controller = map.controller
                controller.setZoom(5.0)

                val startPoint = GeoPoint(28.6139, 77.2090) // India default
                controller.setCenter(startPoint)

                // 🚗 DRIVER MARKER
                val dMarker = Marker(map)
                dMarker.position = startPoint
                dMarker.title = "Driver"
                map.overlays.add(dMarker)
                driverMarker = dMarker

                // 🔵 USER MARKER (only if available)
                if (userLat != 0.0 && userLng != 0.0) {
                    val uMarker = Marker(map).apply {
                        position = GeoPoint(userLat, userLng)
                        title = "You"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    map.overlays.add(uMarker)
                    userMarker = uMarker
                }

                mapView = map
                map
            }
        )
    }

    // 🔹 SOCKET DRIVER LOCATION
    LaunchedEffect(Unit) {
        SocketHandler.joinRide(rideId)

        SocketHandler.listenDriverLocation { lat, lng ->
            Log.d("SOCKET", "Lat: $lat Lng: $lng")
            driverLat = lat
            driverLng = lng
        }
    }

    // 🔹 DRIVER LIVE UPDATE
    LaunchedEffect(driverLat, driverLng) {

        val point = GeoPoint(driverLat, driverLng)

        driverMarker?.position = point

        if (driverLat != 0.0 && driverLng != 0.0) {
            mapView?.controller?.setCenter(point)
        }

        mapView?.invalidate()

        driverAddress = getAddressFromLatLng(driverLat, driverLng)
    }

    // 🔹 USER LIVE UPDATE
    LaunchedEffect(userLat, userLng) {

        val point = GeoPoint(userLat, userLng)

        if (userMarker == null && mapView != null) {
            val uMarker = Marker(mapView)
            uMarker.position = point
            uMarker.title = "You"
            uMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            mapView?.overlays?.add(uMarker)
            userMarker = uMarker
        } else {
            userMarker?.position = point
        }

        mapView?.invalidate()
    }
}
