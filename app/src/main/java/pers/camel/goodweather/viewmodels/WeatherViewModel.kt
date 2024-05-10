package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.data.City
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

data class CurrentWeather(
    val temperature: String,
    val description: String,
)

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val qWeatherService: QWeatherService
) : ViewModel() {

    private val _currentCity = MutableStateFlow(City("101010100", "北京", "北京", "北京市", "中国"))
    val currentCity = _currentCity.asStateFlow()

    private val _updateTime = MutableStateFlow(LocalDateTime.MIN)

    private val _updateDuration = MutableStateFlow("从未更新")
    val updateDuration = _updateDuration.asStateFlow()

    private val _currentWeather = MutableStateFlow(CurrentWeather("--", ""))
    val currentWeather = _currentWeather.asStateFlow()

    private val _firstLoad = MutableStateFlow(true)
    val firstLoad = _firstLoad.asStateFlow()

    suspend fun updateCurrentWeather(cityId: String) {
        val response = qWeatherService.getCurrentWeather(cityId).now
        if (response == null) {
            _currentWeather.value = CurrentWeather("--", "")
            return
        }
        _currentWeather.value = CurrentWeather(response.temp, response.text)
        _updateTime.value = LocalDateTime.now()
        updateUpdateDuration()
    }

    fun updateUpdateDuration() {
        val duration = Duration.between(_updateTime.value, LocalDateTime.now()).toMinutes()
        _updateDuration.value = when {
            duration < 1 -> "刚刚更新"
            duration < 60 -> "${duration}分钟前"
            duration < 1440 -> "${duration / 60}小时前"
            else -> "${duration / 1440}天前"
        }
    }

    fun setFirstLoad(firstLoad: Boolean) {
        _firstLoad.value = firstLoad
    }
}

data class Forecast(
    val date: String,
    val temperatureMax: Int,
    val temperatureMin: Int,
    val description: String,
)

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val qWeatherService: QWeatherService
) : ViewModel() {
    private val _forecasts = MutableStateFlow<List<Forecast>>(
        emptyList()
    )
    val forecasts = _forecasts.asStateFlow()

    suspend fun updateForecast(cityId: String) {
        val response = qWeatherService.getForecast(cityId).daily
        if (response == null) {
            _forecasts.value = emptyList()
            return
        }
        _forecasts.value = response.map {
            Forecast(
                it.fxDate,
                it.tempMax.toInt(),
                it.tempMin.toInt(),
                it.textDay
            )
        }
    }
}