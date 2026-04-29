package com.yourcompany.pumpmanager.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.hilt.navigation.compose.hiltViewModel
import com.yourcompany.pumpmanager.feature.auth.AuthScreen
import com.yourcompany.pumpmanager.feature.dashboard.DashboardScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState,
    startDestination: String = Routes.Auth.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.Splash.route) {
            Text("Splash Screen")
        }
        composable(Routes.Auth.route) {
            AuthScreen(
                viewModel = hiltViewModel(),
                snackbarHostState = snackbarHostState,
                onAuthSuccess = {
                    navController.navigate(Routes.Dashboard.route) {
                        popUpTo(Routes.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Dashboard.route) {
            DashboardScreen(
                snackbarHostState = snackbarHostState,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }
    }
}
