package pers.camel.goodweather.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.data.City
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CityListViewModel
import pers.camel.goodweather.viewmodels.CityViewModel

@Composable
fun AddCityScreen(
    cityListViewModel: CityListViewModel,
    cityViewModel: CityViewModel,
    onBackClick: () -> Unit
) {
    val cities by cityListViewModel.cities.collectAsState()
    val loading by cityListViewModel.loading.collectAsState()
    val error by cityListViewModel.error.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(onBackClick) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                cityListViewModel = cityListViewModel
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    items(cities, key = { it.id }) { city ->
                        CityItem(city) {
                            cityViewModel.addCity(city)
                            onBackClick()
                        }
                    }
                }
                if (loading) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "正在搜索...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                if (error) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = "搜索失败",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "添加城市",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "返回"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun SearchBar(modifier: Modifier, cityListViewModel: CityListViewModel) {
    var text by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                if (it.isNotEmpty()) {
                    coroutineScope.launch {
                        cityListViewModel.searchCity(text)
                    }
                } else {
                    cityListViewModel.setCities(emptyList())
                }
            },
            placeholder = { Text("请输入城市名") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索"
                )
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { text = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "清空"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (text.isNotEmpty()) {
                        coroutineScope.launch {
                            cityListViewModel.searchCity(text)
                        }
                    } else {
                        cityListViewModel.setCities(emptyList())
                    }
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CityItem(city: City, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = "${city.name}, ${city.adm2}, ${city.adm1}, ${city.country}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddCityScreenPreview() {
    val context = LocalContext.current
    val cityListViewModel = CityListViewModel(QWeatherService(context))
    cityListViewModel.setCities(
        listOf(
            City("1", "北京", "北京", "北京", "中国"),
            City("2", "上海", "上海", "上海", "中国"),
            City("3", "广州", "广州", "广东", "中国"),
            City("4", "深圳", "深圳", "广东", "中国"),
        )
    )
    val cityViewModel = CityViewModel()
    GoodWeatherTheme {
        AddCityScreen(cityListViewModel = cityListViewModel, cityViewModel) {}
    }
}