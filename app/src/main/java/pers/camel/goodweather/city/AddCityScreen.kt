package pers.camel.goodweather.city

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CityListViewModel

@Composable
fun AddCityScreen(cityListViewModel: CityListViewModel, onBackClick: () -> Unit) {
    val cities by cityListViewModel.cities.collectAsState()

    Scaffold(topBar = { TopBar(onBackClick) }) { innerPadding ->
        SearchBar(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            cityListViewModel = cityListViewModel
        )
        Text(text = cities, modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "添加城市",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
private fun SearchBar(modifier: Modifier, cityListViewModel: CityListViewModel) {
    var text by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    TextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier,
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
                coroutineScope.launch {
                    cityListViewModel.searchCity(text)
                }
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AddCityScreenPreview() {
    val context = LocalContext.current
    val cityListViewModel = CityListViewModel(QWeatherService(context))
    GoodWeatherTheme {
        AddCityScreen(cityListViewModel = cityListViewModel) {}
    }
}