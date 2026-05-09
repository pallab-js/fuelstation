package com.pallab.pumpmanager.feature.auth

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_shows_welcomeMessage() {
        composeTestRule.setContent {
            AuthContent(
                state = AuthUiState(),
                onEvent = {},
                isBiometricAvailable = false
            )
        }
        composeTestRule.onNodeWithText("Welcome Back").assertExists()
    }
}
