package pers.camel.goodweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pers.camel.goodweather.city.AddCityScreen
import pers.camel.goodweather.city.CityScreen
import pers.camel.goodweather.main.MainScreen
import pers.camel.goodweather.ui.theme.GoodWeatherTheme
import pers.camel.goodweather.viewmodels.CityViewModel
import pers.camel.goodweather.viewmodels.CurrentWeatherViewModel
import pers.camel.goodweather.viewmodels.ForecastViewModel
import pers.camel.goodweather.viewmodels.SearchCityResultViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val enterTransition: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideIntoContainer(
                animationSpec = tween(300, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    private val popExitTransition: @JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            slideOutOfContainer(
                animationSpec = tween(300, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val cityViewModel: CityViewModel by viewModels()

        setContent {
            GoodWeatherTheme {
                val navController = rememberNavController()
                NavHost(
                    navController,
                    startDestination = "main",
                ) {
                    composable(route = "main", enterTransition = { EnterTransition.None },
                        popEnterTransition = { EnterTransition.None }) {
                        val currentWeatherViewModel = hiltViewModel<CurrentWeatherViewModel>()
                        val forecastViewModel = hiltViewModel<ForecastViewModel>()
                        MainScreen(currentWeatherViewModel, forecastViewModel, onCityClick = {
                            navController.navigate("city")
                        })
                    }
                    composable(
                        route = "city",
                        enterTransition = enterTransition,
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = popExitTransition
                    ) {
                        CityScreen(cityViewModel, onBackClick = {
                            navController.popBackStack()
                        }, onAddCityClick = {
                            navController.navigate("addCity")
                        })
                    }
                    composable(
                        route = "addCity",
                        enterTransition = enterTransition,
                        popExitTransition = popExitTransition,
                    ) {
                        val searchCityResultViewModel = hiltViewModel<SearchCityResultViewModel>()
                        AddCityScreen(searchCityResultViewModel, cityViewModel) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}