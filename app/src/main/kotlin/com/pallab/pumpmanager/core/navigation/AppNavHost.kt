package com.pallab.pumpmanager.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pallab.pumpmanager.feature.auth.AuthScreen
import com.pallab.pumpmanager.feature.dashboard.DashboardScreen
import com.pallab.pumpmanager.feature.fuelprices.FuelPricesScreen
import com.pallab.pumpmanager.feature.inventory.InventoryScreen
import com.pallab.pumpmanager.feature.saleshistory.SalesHistoryScreen
import com.pallab.pumpmanager.feature.reports.ReportsScreen
import com.pallab.pumpmanager.feature.sales.SalesScreen
import com.pallab.pumpmanager.feature.shift.ShiftScreen
import com.pallab.pumpmanager.feature.splash.SetPinScreen
import com.pallab.pumpmanager.feature.splash.SplashScreen
import com.pallab.pumpmanager.feature.splash.SplashState
import com.pallab.pumpmanager.feature.splash.SplashViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
    ) {
        composable<SplashRoute>(
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            val splashState by splashViewModel.state.collectAsState()

            LaunchedEffect(splashState) {
                when (splashState) {
                    SplashState.HasPin -> navController.navigate(AuthRoute) { popUpTo<SplashRoute> { inclusive = true } }
                    SplashState.NeedsPinSetup -> navController.navigate(SetPinRoute) { popUpTo<SplashRoute> { inclusive = true } }
                    else -> {}
                }
            }

            SplashScreen()
        }
        composable<AuthRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            AuthScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState,
                onAuthSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<AuthRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<SetPinRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            SetPinScreen(
                viewModel = hiltViewModel(),
                onPinSet = {
                    navController.navigate(AuthRoute) {
                        popUpTo<SetPinRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<DashboardRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            DashboardScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToFuelPrices = { navController.navigate(FuelPricesRoute) },
                onNavigateToSalesHistory = { navController.navigate(SalesHistoryRoute) }
            )
        }
        composable<SalesRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            SalesScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<ShiftRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            ShiftScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<ReportsRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            ReportsScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<InventoryRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            InventoryScreen(viewModel = hiltViewModel(), onNavigateToFuelPrices = { navController.navigate(FuelPricesRoute) })
        }
        composable<FuelPricesRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            FuelPricesScreen(onBack = { navController.popBackStack() })
        }
        composable<SalesHistoryRoute>(
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it / 4 } }
        ) {
            SalesHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
