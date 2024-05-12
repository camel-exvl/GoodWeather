package pers.camel.goodweather.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Reference: https://github.com/munbonecci/GetLocationCompose
class LocationPermission(private val context: Context) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestLocationPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onPermissionsRevoked: () -> Unit
    ) {
        // Initialize the state for managing multiple location permissions.
        val permissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // Check if all previously granted permissions are revoked.
        val allPermissionsRevoked =
            permissionState.permissions.size == permissionState.revokedPermissions.size

        // Filter permissions that need to be requested.
        val permissionsToRequest = permissionState.permissions.filter {
            !it.status.isGranted
        }

        // If there are permissions to request, launch the permission request.
        if (permissionsToRequest.isNotEmpty()) {
            LaunchedEffect(true) {
                permissionState.launchMultiplePermissionRequest()
            }
        }

        // Execute callbacks based on permission status.
        if (allPermissionsRevoked) {
            onPermissionsRevoked()
        } else {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit,
        onGetLastLocationIsNull: () -> Unit
    ) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the last known location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location == null) {
                        onGetLastLocationIsNull()
                    } else {
                        onGetLastLocationSuccess(Pair(location.latitude, location.longitude))
                    }
                }
                .addOnFailureListener { exception ->
                    // If an error occurs, invoke the failure callback with the exception
                    onGetLastLocationFailed(exception)
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        onGetCurrentLocationIsNull: () -> Unit,
        priority: Boolean = true
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            val currentLocationRequest =
                CurrentLocationRequest.Builder()
                    .setPriority(accuracy)
                    .setGranularity(Granularity.GRANULARITY_COARSE)
                    .build()
            fusedLocationClient.getCurrentLocation(
                currentLocationRequest, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                if (location == null) {
                    onGetCurrentLocationIsNull()
                } else {
                    onGetCurrentLocationSuccess(Pair(location.latitude, location.longitude))
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}