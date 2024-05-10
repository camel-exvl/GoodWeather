package pers.camel.goodweather.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pers.camel.goodweather.R
import pers.camel.goodweather.api.QWeatherService
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CurrentWeatherViewModel
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
    val pullToRefreshState = rememberPullToRefreshState()
    val firstLoad by currentWeatherViewModel.firstLoad.collectAsState()

    if (firstLoad) {
        pullToRefreshState.startRefresh()
        currentWeatherViewModel.setFirstLoad(false)
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            currentWeatherViewModel.updateCurrentWeather(city.id)
            forecastViewModel.updateForecast(city.id)
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        containerColor = Color.Gray,
        topBar = {
            TopBar(Modifier, currentWeatherViewModel, onCityClick)
        },
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CurrentWeather(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 96.dp)
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
                    Forecast(
                        modifier = Modifier, viewModel = forecastViewModel
                    )
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    viewModel: CurrentWeatherViewModel,
    onCityClick: () -> Unit = {}
) {
    val city by viewModel.currentCity.collectAsState()
    val weather by viewModel.currentWeather.collectAsState()
    val updateDurationValue by viewModel.updateDuration.collectAsState()

    LaunchedEffect(true) {
        Timer().schedule(60000, 60000) {
            viewModel.updateUpdateDuration()
        }
    }

    TopAppBar(
        title = {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = city.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Row(
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
            }
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Gray)
    )
}

@Composable
private fun CurrentWeather(
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
private fun Forecast(
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
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        forecasts.forEach {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = it.date,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = it.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${it.temperatureMin}°C/${it.temperatureMax}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
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
    GoodWeatherTheme {
        MainScreen(currentWeatherViewModel, forecastViewModel) {}
    }
}