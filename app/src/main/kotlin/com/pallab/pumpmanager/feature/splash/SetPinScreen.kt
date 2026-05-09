package com.pallab.pumpmanager.feature.splash

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
            text = "Choose a 4-digit PIN to secure your account",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // First PIN row
        Text(
            text = if (state.isConfirming) "Confirm PIN" else "Enter PIN",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < state.pinInput.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                )
            }
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("1", "2", "3").forEach { digit ->
                Button(
                    onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) },
                    modifier = Modifier.width(80.dp).height(64.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text(digit, style = MaterialTheme.typography.titleLarge) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("4", "5", "6").forEach { digit ->
                Button(
                    onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) },
                    modifier = Modifier.width(80.dp).height(64.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text(digit, style = MaterialTheme.typography.titleLarge) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("7", "8", "9").forEach { digit ->
                Button(
                    onClick = { viewModel.onEvent(SetPinEvent.DigitEntered(digit)) },
                    modifier = Modifier.width(80.dp).height(64.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text(digit, style = MaterialTheme.typography.titleLarge) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { viewModel.onEvent(SetPinEvent.Delete) },
                modifier = Modifier.width(80.dp).height(64.dp),
                shape = MaterialTheme.shapes.small
            ) { Text("DEL") }
            Button(
                onClick = { viewModel.onEvent(SetPinEvent.DigitEntered("0")) },
                modifier = Modifier.width(80.dp).height(64.dp),
                shape = MaterialTheme.shapes.small
            ) { Text("0", style = MaterialTheme.typography.titleLarge) }
            OutlinedButton(
                onClick = { /* skip - will be disabled */ },
                modifier = Modifier.width(80.dp).height(64.dp),
                shape = MaterialTheme.shapes.small,
                enabled = false
            ) { Text("") }
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
