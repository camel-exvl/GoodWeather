package pers.camel.goodweather.city

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import pers.camel.goodweather.data.City
import pers.camel.goodweather.draggable.DeleteAction
import pers.camel.goodweather.draggable.DragAnchors
import pers.camel.goodweather.draggable.DraggableItem
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CityViewModel
import kotlin.math.roundToInt

@Composable
fun CityScreen(cityViewModel: CityViewModel, onBackClick: () -> Unit, onAddCityClick: () -> Unit) {
    val cities by cityViewModel.cities.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(onBackClick, onAddCityClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(cities, key = { it.id }) { city ->
                CityItem(city.name)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CityItem(city: String) {
    val density = LocalDensity.current
    val defaultActionSize = 80.dp
    val endActionSizePx = with(density) { (defaultActionSize * 2).toPx() }
//    val startActionSizePx = with(density) { defaultActionSize.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
//                DragAnchors.Start at -startActionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
        )
    }

    DraggableItem(
        modifier = Modifier
            .height(70.dp),
        state = state,
        content = {
            Text(
                modifier = Modifier.padding(16.dp),
                text = city,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        endAction = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
            ) {
                DeleteAction(
                    Modifier
                        .width(defaultActionSize)
                        .fillMaxHeight()
                        .offset {
                            IntOffset(
                                (-state
                                    .requireOffset() + endActionSizePx)
                                    .roundToInt(), 0
                            )
                        }
                )
            }
        }
    )
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