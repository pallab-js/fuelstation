package com.pallab.pumpmanager.feature.splash

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pallab.pumpmanager.feature.auth.PinDot
import com.pallab.pumpmanager.feature.auth.PinKey

@Composable
fun SetPinScreen(
    viewModel: SetPinViewModel = hiltViewModel(),
    onPinSet: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isPinSet) {
        if (state.isPinSet) onPinSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Set Your PIN",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (state.isConfirming) "Confirm your PIN" else "Choose a 4-digit PIN",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StepperDot(active = true, label = "Set")
            Box(
                Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(
                        if (state.isConfirming) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
            )
            StepperDot(active = state.isConfirming, label = "Confirm")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (state.isConfirming) "Confirm PIN (Step 2 of 2)" else "Set PIN (Step 1 of 2)",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                PinDot(filled = index < state.pinInput.length)
            }
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
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

        // Numpad
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("1", "2", "3").forEach { digit ->
                PinKey(text = digit, onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("4", "5", "6").forEach { digit ->
                PinKey(text = digit, onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("7", "8", "9").forEach { digit ->
                PinKey(text = digit, onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(64.dp))
            PinKey(text = "0", onClick = { viewModel.onEvent(SetPinEvent.DigitEntered("0")) })
            PinKey(icon = Icons.AutoMirrored.Filled.Backspace, onClick = { viewModel.onEvent(SetPinEvent.Delete) })
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun StepperDot(active: Boolean, label: String) {
    val color by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "stepper"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
