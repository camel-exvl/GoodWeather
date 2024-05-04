package pers.camel.goodweather

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

data class CurrentWeather(
    val temperature: Int,
    val description: String,
    val updateTime: LocalDateTime
)

class CurrentWeatherViewModel : ViewModel() {
    private val _currentWeather = MutableStateFlow(CurrentWeather(25, "晴", LocalDateTime.now()))
    val currentWeather = _currentWeather.asStateFlow()
}

data class Forecast(
    val date: String,
    val temperatureMax: Int,
    val temperatureMin: Int,
    val description: String,
)

class ForecastViewModel : ViewModel() {
    private val _forecasts = MutableStateFlow(
        listOf(
            Forecast("5/3", 30, 20, "晴"),
            Forecast("5/4", 31, 21, "晴"),
            Forecast("5/5", 32, 22, "晴"),
            Forecast("5/6", 32, 22, "晴"),
            Forecast("5/7", 32, 22, "晴"),
            Forecast("5/8", 32, 22, "晴"),
            Forecast("5/9", 32, 22, "晴"),
        )
    )
    val forecasts = _forecasts.asStateFlow()
}