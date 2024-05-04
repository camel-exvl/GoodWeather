package pers.camel.goodweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import java.time.Duration
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentWeatherViewModel: CurrentWeatherViewModel by viewModels()
        val forecastViewModel: ForecastViewModel by viewModels()
        setContent {
            GoodWeatherTheme {
                Scaffold(
                    containerColor = Color.Gray,
                    topBar = {
                        TopBar(Modifier.padding(16.dp), currentWeatherViewModel)
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        CurrentWeather(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 96.dp), viewModel = currentWeatherViewModel
                        )
                        Card(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            )
                        ) {
                            Forecast(
                                modifier = Modifier, viewModel = forecastViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    viewModel: CurrentWeatherViewModel
) {
    val weather by viewModel.currentWeather.collectAsState()
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
                text = "北京",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = {}) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.apartment_fill0_wght400_grad0_opsz24),
                    contentDescription = "城市选择",
                    tint = Color.White
                )
            }
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
                text = "${
                    Duration.between(weather.updateTime, LocalDateTime.now()).toMinutes()
                }分钟前",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
fun CurrentWeather(
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
fun Forecast(
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
fun GreetingPreview() {
}