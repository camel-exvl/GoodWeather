package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.R
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.data.City
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    fun setCurrentWeather(currentWeather: CurrentWeather) {
        _currentWeather.value = currentWeather
    }

    fun setCurrentCity(city: City) {
        _currentCity.value = city
    }
}

data class Forecast(
    val date: String,
    val temperatureMax: Int,
    val temperatureMin: Int,
    val descriptionDay: String,
    val imageDay: Int,
    val descriptionNight: String,
    val imageNight: Int,
    val windDirDay: String,
    val windScaleDay: String,
    val windDirNight: String,
    val windScaleNight: String,
)

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val qWeatherService: QWeatherService
) : ViewModel() {
    private val _forecasts = MutableStateFlow<List<Forecast>>(
        emptyList()
    )
    val forecasts = _forecasts.asStateFlow()

    private val _showDialog = MutableStateFlow<Forecast?>(null)
    val showDialog = _showDialog.asStateFlow()

    suspend fun updateForecast(cityId: String) {
        val response = qWeatherService.getForecast(cityId).daily
        if (response == null) {
            _forecasts.value = emptyList()
            return
        }
        _forecasts.value = response.map {
            Forecast(
                LocalDate.parse(it.fxDate).format(DateTimeFormatter.ofPattern("M/d")),
                it.tempMax.toInt(),
                it.tempMin.toInt(),
                it.textDay,
                getImageId(it.iconDay.toInt()),
                it.textNight,
                getImageId(it.iconNight.toInt()),
                it.windDirDay,
                it.windScaleDay,
                it.windDirNight,
                it.windScaleNight
            )
        }
    }

    fun getImageId(imageId: Int): Int {
        return when (imageId) {
            100 -> R.drawable.weather_clear_day_pixel
            101, 102, 103 -> R.drawable.weather_partly_cloudy_day_pixel
            104 -> R.drawable.weather_cloudy_pixel
            150 -> R.drawable.weather_clear_night_pixel
            151, 152, 153 -> R.drawable.weather_partly_cloudy_night_pixel
            302, 303 -> R.drawable.weather_thunderstorm_pixel
            304 -> R.drawable.weather_hail_pixel
            in 300..399 -> R.drawable.weather_rain_pixel
            404, 405, 406 -> R.drawable.weather_sleet_pixel
            in 400..499 -> R.drawable.weather_snow_pixel
            500, 501, 509, 510, 514, 515 -> R.drawable.weather_fog_pixel
            in 503..508 -> R.drawable.weather_wind_pixel
            in 500..599 -> R.drawable.weather_haze_pixel
            else -> R.drawable.weather_cloudy_pixel
        }
    }

    fun setShowDialog(forecast: Forecast?) {
        _showDialog.value = forecast
    }

    fun setForecasts(forecasts: List<Forecast>) {
        _forecasts.value = forecasts
    }
}