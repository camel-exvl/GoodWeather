package pers.camel.goodweather.data

import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherResponse(
    val code: Int,
    val updateTime: String? = null,
    val fxLink: String? = null,
    val now: Now? = null,
    val refer: Refer? = null
)

@Serializable
data class Now(
    val obsTime: String,
    val temp: String,
    val feelsLike: String,
    val icon: Int,
    val text: String,
    val wind360: String,
    val windDir: String,
    val windScale: String,
    val windSpeed: String,
    val humidity: String,
    val precip: String,
    val pressure: String,
    val vis: String,
    val cloud: String,
    val dew: String
)