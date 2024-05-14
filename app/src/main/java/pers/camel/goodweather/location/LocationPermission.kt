package pers.camel.goodweather.location

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.rx3.awaitFirst
import pers.camel.goodweather.data.LocationData

open class LocationPermission(
    private val context: Context,
    private val locationService: LocationService
) {

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    open fun RequestLocationPermission(
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
        LaunchedEffect(permissionState.revokedPermissions) {
            // If there are permissions to request, launch the permission request.
            if (permissionsToRequest.isNotEmpty()) {
                permissionState.launchMultiplePermissionRequest()
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
    }

    suspend fun getLocation(): LocationData {
        try {
            return locationService.requestLocation(context).awaitFirst()
        } catch (e: Exception) {
            throw e
        }
    }
}