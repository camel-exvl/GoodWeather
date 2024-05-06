package pers.camel.goodweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pers.camel.goodweather.ui.theme.GoodWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val currentWeatherViewModel: CurrentWeatherViewModel by viewModels()
        val forecastViewModel: ForecastViewModel by viewModels()
        setContent {
            GoodWeatherTheme {
                val navController = rememberNavController()
                NavHost(
                    navController,
                    startDestination = "main"
                ) {
                    composable(route = "main", popEnterTransition = { EnterTransition.None }) {
                        MainScreen(currentWeatherViewModel, forecastViewModel, onCityClick = {
                            navController.navigate("city")
                        })
                    }
                    composable(
                        route = "city",
                        enterTransition = {
                            slideIntoContainer(
                                animationSpec = tween(300, easing = EaseIn),
                                towards = AnimatedContentTransitionScope.SlideDirection.Start
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                animationSpec = tween(300, easing = EaseOut),
                                towards = AnimatedContentTransitionScope.SlideDirection.End
                            )
                        }
                    ) {
                        CityScreen(CityViewModel(), onBackClick = {
                            navController.popBackStack()
                        })
                    }
                }
            }
        }
    }
}