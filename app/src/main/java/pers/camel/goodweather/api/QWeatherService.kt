package pers.camel.goodweather.api

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import pers.camel.goodweather.R
import pers.camel.goodweather.data.CitySearchResponse

class QWeatherService(context: Context) {
    companion object {
        private const val TAG = "QWeatherService"
        private const val WEATHER_URL = "https://devapi.qweather.com"
        private const val GEO_URL = "https://geoapi.qweather.com"
        private lateinit var key: String
        private val client = HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    init {
        key = context.getString(R.string.qweather_key)
    }

    suspend fun getCity(name: String): CitySearchResponse {
        try {
            val response = client.get("$GEO_URL/v2/city/lookup?location=$name&number=20&key=$key")
            return response.body()
        } catch (e: Exception) {
            Log.e(TAG, "getCity error: $e")
            return CitySearchResponse(0, null, null)
        }
    }
}