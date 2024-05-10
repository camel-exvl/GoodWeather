package pers.camel.goodweather.city

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pers.camel.goodweather.data.City
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CityViewModel

@Composable
fun CityScreen(cityViewModel: CityViewModel, onBackClick: () -> Unit, onAddCityClick: () -> Unit) {
    val cities by cityViewModel.cities.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(onBackClick, onAddCityClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 8.dp)
        ) {
            items(cities, key = { it.id }) { city ->
                CityItem(city.name) {
                    cityViewModel.removeCity(city)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBackClick: () -> Unit, onAddCityClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "城市列表",
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
        actions = {
            IconButton(onClick = onAddCityClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "添加城市")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun CityItem(city: String, onDelete: () -> Unit) {
    SwipeBox(
        modifier = Modifier
            .fillMaxSize(),
        onDelete = onDelete
    ) {
        ListItem(headlineContent = {
            Text(
                modifier = Modifier.padding(16.dp),
                text = city,
                style = MaterialTheme.typography.bodyMedium
            )
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBox(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        positionalThreshold = { distance -> distance * 0.75f },
    )

    SwipeToDismissBox(
        modifier = modifier.animateContentSize(),
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = Icons.Outlined.Delete, contentDescription = "删除"
                )
            }
        }
    ) {
        content()
    }

    if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        onDelete()
    }
}

@Preview(showBackground = true)
@Composable
fun CityScreenPreview() {
    val cityViewModel = CityViewModel()
    cityViewModel.setCities(
        listOf(
            City("1", "北京", "北京", "北京", "中国"),
            City("2", "上海", "上海", "上海", "中国"),
            City("3", "广州", "广州", "广东", "中国"),
            City("4", "深圳", "深圳", "广东", "中国"),
        )
    )
    GoodWeatherTheme {
        CityScreen(cityViewModel, {}) {}
    }
}