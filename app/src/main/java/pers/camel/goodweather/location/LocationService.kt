package pers.camel.goodweather.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import pers.camel.goodweather.data.LocationData
import kotlin.time.Duration.Companion.seconds

open class LocationException(message: String, cause: Throwable) : Exception(message, cause)
class GPSDisabledException : LocationException("GPS is disabled", SecurityException())

// Ref: https://github.com/breezy-weather/breezy-weather/blob/main/app/src/main/java/org/breezyweather/sources/android/AndroidLocationService.kt
@SuppressLint("MissingPermission")
class LocationService(private val context: Context) : LocationListenerCompat {

    private lateinit var locationManager: LocationManager
    private var androidLocation: Location? = null

    private val bestProvider: String
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    locationManager.allProviders.contains(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER

            locationManager.allProviders.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

    fun requestLocation(context: Context): Observable<LocationData> {
        if (!areLocationPermissionsGranted()) {
            Log.w(TAG, "Location permissions not granted")
            throw LocationException("Location permissions not granted", SecurityException())
        }
        if (!this::locationManager.isInitialized) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            Log.w(TAG, "Location is disabled")
            throw GPSDisabledException()
        }

        return rxObservable {
            androidLocation = null
            clearLocationUpdates()

            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                bestProvider,
                LocationRequestCompat.Builder(1000).apply {
                    setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                }.build(),
                this@LocationService,
                Looper.getMainLooper()
            )

            // Dirty, should be improved
            // wait X seconds for callbacks to set locations
            for (i in 1..TIMEOUT_MILLIS / 1000) {
                delay(1000)

                if (androidLocation != null) {
                    clearLocationUpdates()
                    send(LocationData(androidLocation!!.latitude, androidLocation!!.longitude))
                    break
                }
            }

            if (androidLocation == null) {
                clearLocationUpdates()
                getLastKnownLocation(locationManager)?.let {
                    send(LocationData(it.latitude, it.longitude))
                } ?: run {
                    // Actually it’s a timeout, but it is more reasonable to say it failed to find location
                    Log.e(TAG, "Failed to find location")
                    throw LocationException("Failed to find location", SecurityException())
                }
            }
        }.doOnDispose {
            clearLocationUpdates()
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun clearLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    // location listener.
    override fun onLocationChanged(location: Location) {
        clearLocationUpdates()
        androidLocation = location
        Log.i(TAG, "Got GPS location")
    }

    override fun onProviderEnabled(provider: String) {
        // do nothing.
    }

    override fun onProviderDisabled(provider: String) {
        // do nothing.
    }

    companion object {
        private const val TAG = "LocationService"
        private val TIMEOUT_MILLIS = 10.seconds.inWholeMilliseconds

        @SuppressLint("MissingPermission")
        private fun getLastKnownLocation(
            locationManager: LocationManager
        ): Location? {
            val lastKnownFused = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            } else null
            return lastKnownFused
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        }
    }
}