package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.data.City


class CityViewModel : ViewModel() {
    private val _cities =
        MutableStateFlow<List<City>>(listOf(City("101010100", "北京", "北京", "北京市", "中国")))
    val cities = _cities.asStateFlow()

    fun addCity(city: City) {
        _cities.value += city
    }

    fun setCities(cities: List<City>) {
        _cities.value = cities
    }
}