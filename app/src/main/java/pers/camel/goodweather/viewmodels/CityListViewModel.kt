package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.data.City
import javax.inject.Inject

@HiltViewModel
class CityListViewModel @Inject constructor(
    private val qWeatherService: QWeatherService
) : ViewModel() {

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities = _cities.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    suspend fun searchCity(name: String) {
        _loading.value = true
        _error.value = false
        val response = qWeatherService.getCity(name).location
        _loading.value = false
        if (response == null) {
            _cities.value = emptyList()
            _error.value = true
            return
        }
        _cities.value = response.map { City(it.id, it.name, it.adm2, it.adm1, it.country) }
    }

    fun setCities(cities: List<City>) {
        _cities.value = cities
    }
}