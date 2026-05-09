package com.pallab.pumpmanager.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Genesis Typography Scale — adapted for Compose sp units
// substitute General Sans with DM Sans Bold at tight tracking for Android as suggested in blueprint
val AppTypography = Typography(
    displayLarge  = TextStyle(
        fontSize = 48.sp, 
        fontWeight = FontWeight.Bold,   
        letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontSize = 36.sp, 
        fontWeight = FontWeight.Bold,   
        letterSpacing = (-1.2).sp
    ),
    headlineLarge = TextStyle(
        fontSize = 28.sp, 
        fontWeight = FontWeight.SemiBold, 
        letterSpacing = (-0.8).sp
    ),
    headlineMedium= TextStyle(
        fontSize = 22.sp, 
        fontWeight = FontWeight.SemiBold, 
        letterSpacing = (-0.5).sp
    ),
    titleLarge    = TextStyle(
        fontSize = 18.sp, 
        fontWeight = FontWeight.Medium
    ),
    bodyLarge     = TextStyle(
        fontSize = 15.sp, 
        fontWeight = FontWeight.Normal
    ),
    bodyMedium    = TextStyle(
        fontSize = 13.sp, 
        fontWeight = FontWeight.Normal
    ),
    labelSmall    = TextStyle(
        fontSize = 11.sp, 
        fontWeight = FontWeight.Medium,  
        letterSpacing = 0.5.sp
    )
)
