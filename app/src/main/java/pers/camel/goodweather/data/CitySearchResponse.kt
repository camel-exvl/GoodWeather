package pers.camel.goodweather.data

import kotlinx.serialization.Serializable

@Serializable
data class CitySearchResponse(
    val code: Int,
    val location: List<Location>? = null,
    val refer: Refer? = null
)

@Serializable
data class Location(
    val name: String,
    val id: String,
    val lat: String,
    val lon: String,
    val adm2: String,
    val adm1: String,
    val country: String,
    val tz: String,
    val utcOffset: String,
    val isDst: String,
    val type: String,
    val rank: String,
    val fxLink: String
)