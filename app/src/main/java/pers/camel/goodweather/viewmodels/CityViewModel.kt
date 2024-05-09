package pers.camel.goodweather.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class City(val id: Int, val name: String)

class CityViewModel : ViewModel() {
    private val _cities = MutableStateFlow(
        listOf(
            City(1, "北京"),
            City(2, "上海"),
            City(3, "广州"),
            City(4, "深圳"),
            City(5, "杭州"),
            City(6, "南京"),
            City(7, "成都"),
            City(8, "重庆"),
            City(9, "武汉"),
            City(10, "西安"),
        )
    )
    val cities = _cities.asStateFlow()
}