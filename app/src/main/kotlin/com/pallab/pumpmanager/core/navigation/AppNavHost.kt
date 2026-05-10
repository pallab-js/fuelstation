package com.pallab.pumpmanager.core.navigation

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
        composable<SplashRoute> {
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
        composable<AuthRoute> {
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
        composable<SetPinRoute> {
            SetPinScreen(
                viewModel = hiltViewModel(),
                onPinSet = {
                    navController.navigate(AuthRoute) {
                        popUpTo<SetPinRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<DashboardRoute> {
            DashboardScreen(
                snackbarHostState = snackbarHostState,
                onNavigateToFuelPrices = { navController.navigate(FuelPricesRoute) },
                onNavigateToSalesHistory = { navController.navigate(SalesHistoryRoute) }
            )
        }
        composable<SalesRoute> {
            SalesScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<ShiftRoute> {
            ShiftScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<ReportsRoute> {
            ReportsScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState
            )
        }
        composable<InventoryRoute> {
            InventoryScreen(viewModel = hiltViewModel(), onNavigateToFuelPrices = { navController.navigate(FuelPricesRoute) })
        }
        composable<FuelPricesRoute> {
            FuelPricesScreen(onBack = { navController.popBackStack() })
        }
        composable<SalesHistoryRoute> {
            SalesHistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
