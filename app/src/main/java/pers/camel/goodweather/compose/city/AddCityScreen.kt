package pers.camel.goodweather.compose.city

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import pers.camel.goodweather.viewmodels.CityViewModel
import pers.camel.goodweather.viewmodels.SearchCityResultViewModel

@Composable
fun AddCityScreen(
    searchCityResultViewModel: SearchCityResultViewModel,
    cityViewModel: CityViewModel,
    onBackClick: () -> Unit
) {
    val cities by searchCityResultViewModel.cities.collectAsState()
    val loading by searchCityResultViewModel.loading.collectAsState()
    val error by searchCityResultViewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                searchCityResultViewModel = searchCityResultViewModel
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    items(cities, key = { it.id }) { city ->
                        CityItem(city) {
                            if (cityViewModel.cities.value.contains(city)) {
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        "城市 ${city.name} 已存在",
                                        withDismissAction = true
                                    )
                                }
                            } else {
                                cityViewModel.addCity(city)
                                onBackClick()
                            }
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
private fun SearchBar(modifier: Modifier, searchCityResultViewModel: SearchCityResultViewModel) {
    var text by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                if (it.isNotEmpty()) {
                    coroutineScope.launch {
                        searchCityResultViewModel.searchCity(text)
                    }
                } else {
                    searchCityResultViewModel.setCities(emptyList())
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
                            searchCityResultViewModel.searchCity(text)
                        }
                    } else {
                        searchCityResultViewModel.setCities(emptyList())
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
    val searchCityResultViewModel = SearchCityResultViewModel(QWeatherService(context))
    searchCityResultViewModel.setCities(
        listOf(
            City("1", "北京", "北京", "北京", "中国"),
            City("2", "上海", "上海", "上海", "中国"),
            City("3", "广州", "广州", "广东", "中国"),
            City("4", "深圳", "深圳", "广东", "中国"),
        )
    )
    val cityViewModel = CityViewModel()
    GoodWeatherTheme {
        AddCityScreen(searchCityResultViewModel = searchCityResultViewModel, cityViewModel) {}
    }
}