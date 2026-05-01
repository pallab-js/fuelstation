package com.yourcompany.pumpmanager.feature.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.yourcompany.pumpmanager.core.theme.PumpManagerTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthSuccess()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AuthEvent.DismissError)
        }
    }

    AuthContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBiometricClick = {
            val activity = LocalActivity.current as? FragmentActivity ?: return@AuthContent
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.onEvent(AuthEvent.BiometricTriggered) },
                onError = { /* prompt handles its own error UI */ }
            )
        }
    )
}

@Composable
private fun AuthContent(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onBiometricClick: () -> Unit = { onEvent(AuthEvent.BiometricTriggered) }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your PIN to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // PIN Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                PinIndicator(filled = index < state.pinInput.length)
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Error Message
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom PIN Pad
        PinPad(
            onDigitClick = { onEvent(AuthEvent.PinDigitEntered(it)) },
            onDeleteClick = { onEvent(AuthEvent.PinDeleted) },
            onBiometricClick = onBiometricClick
        )
        
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun PinIndicator(filled: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(
                if (filled) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
    )
}

@Composable
private fun PinPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "DEL")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(280.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(digits) { item ->
            when (item) {
                "BIO" -> PinButton(icon = Icons.Default.Fingerprint, contentDesc = "Authenticate with fingerprint", onClick = onBiometricClick)
                "DEL" -> PinButton(icon = Icons.Default.Backspace, contentDesc = "Delete last digit", onClick = onDeleteClick)
                else -> PinButton(text = item, onClick = { onDigitClick(item) })
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    contentDesc: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    PumpManagerTheme {
        AuthContent(
            state = AuthUiState(pinInput = "12"),
            onEvent = {}
        )
    }
}
