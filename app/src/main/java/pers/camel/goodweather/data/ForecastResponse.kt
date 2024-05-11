package pers.camel.goodweather.data

import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val code: Int,
    val updateTime: String? = null,
    val fxLink: String? = null,
    val daily: List<Daily>? = null,
    val refer: Refer? = null
)

@Serializable
data class Daily(
    val fxDate: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val moonPhase: String,
    val moonPhaseIcon: String,
    val tempMax: String,
    val tempMin: String,
    val iconDay: Int,
    val textDay: String,
    val iconNight: Int,
    val textNight: String,
    val wind360Day: String,
    val windDirDay: String,
    val windScaleDay: String,
    val windSpeedDay: String,
    val wind360Night: String,
    val windDirNight: String,
    val windScaleNight: String,
    val windSpeedNight: String,
    val precip: String,
    val uvIndex: String,
    val humidity: String,
    val pressure: String,
    val vis: String,
    val cloud: String
)