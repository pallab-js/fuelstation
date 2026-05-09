package com.pallab.pumpmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pallab.pumpmanager.core.navigation.AppNavHost
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.theme.PumpManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PumpManagerTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    AppNavHost(
                        modifier = Modifier.padding(paddingValues),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isExpired()) {
            sessionManager.clearAll()
        }
    }
}
