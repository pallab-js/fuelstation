package com.pallab.pumpmanager.feature.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardHome_shows_overview() {
        composeTestRule.setContent {
            DashboardHome(onNavigateToTab = {})
        }
        composeTestRule.onNodeWithText("Overview").assertExists()
    }
}
