package pers.camel.goodweather.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.R
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.data.City
import pers.camel.goodweather.data.LocationData
import pers.camel.goodweather.ui.theme.cloudy
import pers.camel.goodweather.ui.theme.foggy
import pers.camel.goodweather.ui.theme.hail
import pers.camel.goodweather.ui.theme.haze
import pers.camel.goodweather.ui.theme.partlyCloudyDay
import pers.camel.goodweather.ui.theme.partlyCloudyNight
import pers.camel.goodweather.ui.theme.rainy
import pers.camel.goodweather.ui.theme.sleet
import pers.camel.goodweather.ui.theme.snow
import pers.camel.goodweather.ui.theme.sunnyDay
import pers.camel.goodweather.ui.theme.sunnyNight
import pers.camel.goodweather.ui.theme.thunder
import pers.camel.goodweather.ui.theme.wind
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
    private val defaultCity = City("101010100", "北京", "北京", "北京市", "中国")

    private val _currentCity = MutableStateFlow(defaultCity)
    val currentCity = _currentCity.asStateFlow()

    private val _userCity = MutableStateFlow<City?>(null)

    private val _updateTime = MutableStateFlow(LocalDateTime.MIN)

    private val _updateDuration = MutableStateFlow("从未更新")
    val updateDuration = _updateDuration.asStateFlow()

    private val _currentWeather = MutableStateFlow(CurrentWeather("--", ""))
    val currentWeather = _currentWeather.asStateFlow()

    private val _backgroundColor = MutableStateFlow(cloudy)
    val backgroundColor = _backgroundColor.asStateFlow()

    private val _firstLoad = MutableStateFlow(true)
    val firstLoad = _firstLoad.asStateFlow()

    suspend fun updateCurrentWeather(cityId: String): Boolean {
        val response = qWeatherService.getCurrentWeather(cityId).now ?: return false
        try {
            _currentWeather.value = CurrentWeather(response.temp, response.text)
            updateBackgroundColor(response.icon)
            _updateTime.value = LocalDateTime.now()
            updateUpdateDuration()
        } catch (e: Exception) {
            Log.e("CurrentWeatherViewModel", "updateCurrentWeather error: $e")
            return false
        }
        return true
    }

    suspend fun getUserCity(locationData: LocationData, cityViewModel: CityViewModel) {
        val response =
            qWeatherService.getCity("${locationData.longitude},${locationData.latitude}").location
        if (response != null) {
            val city = City(
                response[0].id,
                response[0].name,
                response[0].adm2,
                response[0].adm1,
                response[0].country
            )
            // Only update if the city is different
            if (_userCity.value != city) {
                _userCity.value = city
                _currentCity.value = _userCity.value!!
            }
        } else {
            if (_userCity.value == null) {
                _userCity.value = defaultCity
                _currentCity.value = _userCity.value!!
            }
        }
        cityViewModel.setUserCity(_userCity.value!!)
    }

    fun setUpdateFailed() {
        _updateTime.value = LocalDateTime.MAX
        updateUpdateDuration()
    }

    fun updateUpdateDuration() {
        if (_updateTime.value == LocalDateTime.MAX) {
            _updateDuration.value = "更新失败"
            return
        }
        val duration = Duration.between(_updateTime.value, LocalDateTime.now()).toMinutes()
        _updateDuration.value = when {
            duration < 1 -> "刚刚更新"
            duration < 60 -> "${duration}分钟前"
            duration < 1440 -> "${duration / 60}小时前"
            else -> "${duration / 1440}天前"
        }
    }

    fun updateBackgroundColor(icon: Int) {
        _backgroundColor.value = when (icon) {
            100 -> sunnyDay
            101, 102, 103 -> partlyCloudyDay
            104 -> cloudy
            150 -> sunnyNight
            151, 152, 153 -> partlyCloudyNight
            302, 303 -> thunder
            304 -> hail
            in 300..399 -> rainy
            404, 405, 406 -> sleet
            in 400..499 -> snow
            500, 501, 509, 510, 514, 515 -> foggy
            in 503..508 -> wind
            in 500..599 -> haze
            else -> cloudy
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

    fun showUserCity(): Boolean {
        return _userCity.value == _currentCity.value
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
    companion object {
        private const val TAG = "ForecastViewModel"
    }

    private val _forecasts = MutableStateFlow<List<Forecast>>(
        emptyList()
    )
    val forecasts = _forecasts.asStateFlow()

    private val _showDialog = MutableStateFlow<Forecast?>(null)
    val showDialog = _showDialog.asStateFlow()

    suspend fun updateForecast(cityId: String): Boolean {
        val response = qWeatherService.getForecast(cityId).daily ?: return false
        try {
            _forecasts.value = response.map {
                Forecast(
                    LocalDate.parse(it.fxDate).format(DateTimeFormatter.ofPattern("M/d")),
                    it.tempMax.toInt(),
                    it.tempMin.toInt(),
                    it.textDay,
                    getImageId(it.iconDay),
                    it.textNight,
                    getImageId(it.iconNight),
                    it.windDirDay,
                    it.windScaleDay,
                    it.windDirNight,
                    it.windScaleNight
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateForecast error: $e")
            return false
        }
        return true
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