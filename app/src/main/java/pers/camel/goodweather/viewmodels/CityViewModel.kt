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

    private val _showUserCity = MutableStateFlow(true)
    val showUserCity = _showUserCity.asStateFlow()

    fun addCity(city: City): Boolean {
        if (_allCities.value.contains(city)) {
            return false
        }
        _otherCities.value += city
        updateAllCities()
        return true
    }

    fun removeCity(city: City) {
        if (_userCity.value == city) {
            _showUserCity.value = false
        } else {
            _otherCities.value -= city
        }
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

    fun showUserCity() {
        _showUserCity.value = true
        updateAllCities()
    }

    private fun updateAllCities() {
        if (_showUserCity.value) {
            _allCities.value = (listOf(_userCity.value)) + _otherCities.value
        } else {
            _allCities.value = _otherCities.value
        }
    }

    // ONLY FOR PREVIEW
    @Suppress("unused")
    fun setCities(cities: List<City>) {
        _allCities.value = cities
    }
}