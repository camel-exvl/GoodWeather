package pers.camel.goodweather.compose.main

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pers.camel.goodweather.R
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CurrentWeather
import pers.camel.goodweather.viewmodels.CurrentWeatherViewModel
import pers.camel.goodweather.viewmodels.Forecast
import pers.camel.goodweather.viewmodels.ForecastViewModel
import java.util.Timer
import kotlin.concurrent.schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentWeatherViewModel: CurrentWeatherViewModel,
    forecastViewModel: ForecastViewModel,
    onCityClick: () -> Unit
) {
    val city by currentWeatherViewModel.currentCity.collectAsState()
    val backgroundColor by currentWeatherViewModel.backgroundColor.collectAsState()
    val animatedBackgroundColor = remember { Animatable(backgroundColor) }
    val updateDurationValue by currentWeatherViewModel.updateDuration.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val firstLoad by currentWeatherViewModel.firstLoad.collectAsState()
    val showDialog by forecastViewModel.showDialog.collectAsState()

    if (firstLoad) {
        pullToRefreshState.startRefresh()
        currentWeatherViewModel.setFirstLoad(false)
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            if (!currentWeatherViewModel.updateCurrentWeather(city.id) || !forecastViewModel.updateForecast(
                    city.id
                )
            ) {
                currentWeatherViewModel.setUpdateFailed()
            }
            animatedBackgroundColor.animateTo(backgroundColor, animationSpec = tween(500))
            pullToRefreshState.endRefresh()
        }
    }

    Column(modifier = Modifier.drawBehind {
        drawRect(animatedBackgroundColor.value)
    }) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopBar(currentWeatherViewModel, onCityClick)
            },
            modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_schedule_24),
                            contentDescription = "更新时间",
                            tint = Color.White
                        )
                        Text(
                            text = updateDurationValue,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    CurrentWeatherView(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 72.dp)
                            .fillMaxWidth(),
                        viewModel = currentWeatherViewModel
                    )
                    Card(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    ) {
                        ForecastView(
                            modifier = Modifier, viewModel = forecastViewModel
                        )
                    }
                }

                if (pullToRefreshState.verticalOffset > 0) {
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = pullToRefreshState
                    )
                }

                if (showDialog != null) {
                    ForecastDialog(
                        forecast = showDialog!!,
                        onDismiss = { forecastViewModel.setShowDialog(null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    viewModel: CurrentWeatherViewModel,
    onCityClick: () -> Unit = {}
) {
    val city by viewModel.currentCity.collectAsState()

    LaunchedEffect(true) {
        Timer().schedule(60000, 60000) {
            viewModel.updateUpdateDuration()
        }
    }

    TopAppBar(
        title = {
            Text(
                text = city.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            IconButton(onClick = onCityClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.apartment_fill0_wght400_grad0_opsz24),
                    contentDescription = "城市选择",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun CurrentWeatherView(
    modifier: Modifier = Modifier,
    viewModel: CurrentWeatherViewModel
) {
    val weather by viewModel.currentWeather.collectAsState()
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "${weather.temperature}°C",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = weather.description,
            color = Color.White,
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
private fun ForecastView(
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel
) {
    val forecasts by viewModel.forecasts.collectAsState()
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "每日预报",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall
        )
        forecasts.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.setShowDialog(it)
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = it.date,
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(id = it.imageDay),
                    contentDescription = it.descriptionDay,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "${it.temperatureMin}°C/${it.temperatureMax}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ForecastDialog(
    forecast: Forecast,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = forecast.date,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "白天",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = forecast.imageDay),
                        contentDescription = forecast.descriptionDay,
                        modifier = Modifier.size(64.dp)
                    )
                    Column {
                        Text(
                            text = "${forecast.descriptionDay}, ${forecast.temperatureMax}°C",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${forecast.windDirDay} ${forecast.windScaleDay}级",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    text = "夜晚",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = forecast.imageNight),
                        contentDescription = forecast.descriptionNight,
                        modifier = Modifier.size(64.dp)
                    )
                    Column {
                        Text(
                            text = "${forecast.descriptionNight}, ${forecast.temperatureMin}°C",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${forecast.windDirNight} ${forecast.windScaleNight}级",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val context = LocalContext.current
    val currentWeatherViewModel = CurrentWeatherViewModel(QWeatherService(context))
    val forecastViewModel = ForecastViewModel(QWeatherService(context))
    currentWeatherViewModel.setCurrentWeather(CurrentWeather("20", "晴"))
    currentWeatherViewModel.updateBackgroundColor(100)
    forecastViewModel.setForecasts(
        listOf(
            Forecast(
                "5/1",
                30,
                20,
                "晴",
                forecastViewModel.getImageId(100),
                "晴",
                forecastViewModel.getImageId(150),
                "东北风",
                "3",
                "东风",
                "1-2"
            ),
            Forecast(
                "5/2",
                31,
                21,
                "多云",
                forecastViewModel.getImageId(101),
                "多云",
                forecastViewModel.getImageId(152),
                "东南风",
                "4",
                "南风",
                "3-4"
            ),
            Forecast(
                "5/3",
                32,
                22,
                "阴",
                forecastViewModel.getImageId(104),
                "雾",
                forecastViewModel.getImageId(501),
                "西南风",
                "5",
                "西风",
                "4-5"
            ),
            Forecast(
                "5/4",
                33,
                23,
                "雷阵雨",
                forecastViewModel.getImageId(302),
                "扬尘",
                forecastViewModel.getImageId(504),
                "西北风",
                "6",
                "北风",
                "5-6"
            ),
            Forecast(
                "5/5",
                34,
                24,
                "冰雹",
                forecastViewModel.getImageId(304),
                "霾",
                forecastViewModel.getImageId(502),
                "南风",
                "7",
                "东风",
                "6-7"
            ),
            Forecast(
                "5/6",
                35,
                25,
                "雨夹雪",
                forecastViewModel.getImageId(404),
                "晴",
                forecastViewModel.getImageId(150),
                "东风",
                "8",
                "东风",
                "7-8"
            ),
            Forecast(
                "5/7",
                36,
                26,
                "小雨",
                forecastViewModel.getImageId(303),
                "晴",
                forecastViewModel.getImageId(150),
                "东北风",
                "9",
                "东风",
                "8-9"
            ),
        )
    )
    GoodWeatherTheme {
        MainScreen(currentWeatherViewModel, forecastViewModel) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ForecastDialogPreview() {
    val context = LocalContext.current
    val forecastViewModel = ForecastViewModel(QWeatherService(context))
    val forecast = Forecast(
        "5/1",
        30,
        20,
        "晴",
        forecastViewModel.getImageId(100),
        "晴",
        forecastViewModel.getImageId(150),
        "东北风",
        "3",
        "东风",
        "1-2"
    )
    GoodWeatherTheme {
        ForecastDialog(forecast) {}
    }
}