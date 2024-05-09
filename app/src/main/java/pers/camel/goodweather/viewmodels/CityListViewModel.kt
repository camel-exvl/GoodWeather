package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.api.QWeatherService
import javax.inject.Inject

@HiltViewModel
class CityListViewModel @Inject constructor(
    private val qWeatherService: QWeatherService
) : ViewModel() {

    private val _cities = MutableStateFlow("")
    val cities = _cities.asStateFlow()

    suspend fun searchCity(name: String) {
        _cities.value = qWeatherService.getCity(name)
    }
}