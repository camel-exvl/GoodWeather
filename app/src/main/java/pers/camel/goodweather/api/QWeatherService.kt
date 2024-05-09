package pers.camel.goodweather.api

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import pers.camel.goodweather.R

class QWeatherService(context: Context) {
    companion object {
        private const val WEATHER_URL = "https://devapi.qweather.com"
        private const val GEO_URL = "https://geoapi.qweather.com"
        private lateinit var key: String
        private val client = HttpClient()
    }

    init {
        key = context.getString(R.string.qweather_key)
    }

    suspend fun getCity(name: String): String {
        val response = client.get("$GEO_URL/v2/city/lookup?location=$name&key=$key")
        return response.bodyAsText()
    }
}