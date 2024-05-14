package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pers.camel.goodweather.data.City


class CityViewModel : ViewModel() {
    private val defaultCity = City("101010100", "北京", "北京", "北京市", "中国")

    private val _allCities =
        MutableStateFlow(listOf(defaultCity))
    val cities = _allCities.asStateFlow()

    private val _userCity = MutableStateFlow(defaultCity)

    private val _otherCities = MutableStateFlow<List<City>>(emptyList())

    fun addCity(city: City): Boolean {
        if (_allCities.value.contains(city)) {
            return false
        }
        _otherCities.value += city
        updateAllCities()
        return true
    }

    fun removeCity(city: City) {
        _otherCities.value -= city
        updateAllCities()
    }

    fun setUserCity(city: City) {
        if (_otherCities.value.contains(city)) {
            _otherCities.value -= city
        }
        _userCity.value = city
        updateAllCities()
    }

    fun isUserCity(city: City): Boolean {
        return _userCity.value == city
    }

    private fun updateAllCities() {
        _allCities.value = (listOf(_userCity.value)) + _otherCities.value
    }

    // ONLY FOR PREVIEW
    @Suppress("unused")
    fun setCities(cities: List<City>) {
        _allCities.value = cities
    }
}