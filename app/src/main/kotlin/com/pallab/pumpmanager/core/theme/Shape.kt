package com.pallab.pumpmanager.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Genesis Border Radius Tokens
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Tags, chips, badges, inline values
    small      = RoundedCornerShape(6.dp),   // Buttons, inputs, selects
    medium     = RoundedCornerShape(8.dp),   // Dropdowns, metadata cards, panels
    large      = RoundedCornerShape(12.dp),  // Kit/stat cards, search bar
    extraLarge = RoundedCornerShape(9999.dp) // Avatars, status dots, pill badges
)
