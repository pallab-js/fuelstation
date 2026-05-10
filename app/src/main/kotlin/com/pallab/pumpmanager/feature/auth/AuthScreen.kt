package com.pallab.pumpmanager.feature.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalGasStation
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.pallab.pumpmanager.core.theme.PumpManagerTheme
import java.time.LocalTime

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalActivity.current as? FragmentActivity
    val isBiometricAvailable = activity?.let { BiometricHelper.canAuthenticate(it) } ?: false

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthSuccess()
        }
    }

    if (state.users.size > 1 && state.selectedUserId == null) {
        UserPickerScreen(
            users = state.users,
            onUserSelected = { viewModel.onEvent(AuthEvent.UserSelected(it)) }
        )
    } else {
        AuthContent(
            state = state,
            onEvent = viewModel::onEvent,
            isBiometricAvailable = isBiometricAvailable,
            onBiometricClick = {
                val act = activity ?: return@AuthContent
                BiometricHelper.authenticate(
                    activity = act,
                    onSuccess = { viewModel.onEvent(AuthEvent.BiometricTriggered) },
                    onError = { }
                )
            }
        )
    }
}

@Composable
private fun UserPickerScreen(
    users: List<UserEntity>,
    onUserSelected: (String) -> Unit
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
            text = "Select User",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        users.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onUserSelected(user.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text(user.role.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AuthContent(
    state: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    isBiometricAvailable: Boolean = false,
    onBiometricClick: () -> Unit = { onEvent(AuthEvent.BiometricTriggered) }
) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocalGasStation,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "PumpManager Pro",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "$greeting!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Enter your PIN to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                PinDot(filled = index < state.pinInput.length)
            }
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        PinPad(
            onDigitClick = { onEvent(AuthEvent.PinDigitEntered(it)) },
            onDeleteClick = { onEvent(AuthEvent.PinDeleted) },
            onBiometricClick = onBiometricClick,
            isBiometricAvailable = isBiometricAvailable
        )
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun PinDot(filled: Boolean) {
    val color by animateColorAsState(
        targetValue = if (filled) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(150),
        label = "pinDot"
    )
    Box(
        Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@Composable
private fun PinPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    isBiometricAvailable: Boolean
) {
    val digits = buildList {
        addAll(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9"))
        if (isBiometricAvailable) add("BIO") else add("")
        add("0")
        add("DEL")
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(240.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(digits) { item ->
            when {
                item == "BIO" -> PinKey(icon = Icons.Default.Fingerprint, onClick = onBiometricClick)
                item == "DEL" -> PinKey(icon = Icons.AutoMirrored.Filled.Backspace, onClick = onDeleteClick)
                item.isEmpty() -> Box(modifier = Modifier.size(64.dp))
                else -> PinKey(text = item, onClick = { onDigitClick(item) })
            }
        }
    }
}

@Composable
fun PinKey(text: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (text != null) {
                Text(text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium)
            } else if (icon != null) {
                Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
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
