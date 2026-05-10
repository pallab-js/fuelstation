package com.pallab.pumpmanager.feature.sales

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SalesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val petrol = FuelTypeEntity("petrol", "Petrol", 102.50)
    private val diesel = FuelTypeEntity("diesel", "Diesel", 94.20)

    @Test
    fun salesContent_shows_title() {
        composeTestRule.setContent {
            SalesContent(
                state = SalesUiState(),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("New Sale").assertExists()
    }

    @Test
    fun salesContent_shows_fuelType_chips() {
        composeTestRule.setContent {
            SalesContent(
                state = SalesUiState(fuelTypes = listOf(petrol, diesel)),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("Petrol").assertExists()
        composeTestRule.onNodeWithText("Diesel").assertExists()
    }

    @Test
    fun salesContent_shows_paymentModes() {
        composeTestRule.setContent {
            SalesContent(
                state = SalesUiState(),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("CASH").assertExists()
        composeTestRule.onNodeWithText("UPI").assertExists()
        composeTestRule.onNodeWithText("CARD").assertExists()
    }

    @Test
    fun salesContent_shows_saveButton() {
        composeTestRule.setContent {
            SalesContent(
                state = SalesUiState(),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("Save Sale").assertExists()
    }

    @Test
    fun salesContent_shows_calculatedTotal() {
        composeTestRule.setContent {
            SalesContent(
                state = SalesUiState(
                    fuelTypes = listOf(petrol),
                    selectedFuel = petrol,
                    volume = "10",
                    pricePerLiter = 102.50,
                    calculatedTotal = 1025.0
                ),
                onEvent = {}
            )
        }
        composeTestRule.onNodeWithText("₹ 1025.00").assertExists()
    }
}
