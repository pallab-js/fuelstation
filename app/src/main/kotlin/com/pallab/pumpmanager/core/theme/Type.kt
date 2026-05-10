package com.pallab.pumpmanager.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.pallab.pumpmanager.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val LatoFont = GoogleFont("Lato")

val LatoFamily = FontFamily(
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = LatoFont, fontProvider = provider, weight = FontWeight.Black),
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 52.sp,
        fontWeight   = FontWeight.Black,
        letterSpacing = (-2).sp,
        lineHeight   = 60.sp
    ),
    displayMedium = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 40.sp,
        fontWeight   = FontWeight.Bold,
        letterSpacing = (-1.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 28.sp,
        fontWeight   = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        lineHeight   = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 22.sp,
        fontWeight   = FontWeight.Bold,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 18.sp,
        fontWeight   = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 16.sp,
        fontWeight   = FontWeight.SemiBold,
        lineHeight   = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 14.sp,
        fontWeight   = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 13.sp,
        fontWeight   = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 15.sp,
        fontWeight   = FontWeight.Normal,
        lineHeight   = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 13.sp,
        fontWeight   = FontWeight.Normal,
        lineHeight   = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 12.sp,
        fontWeight   = FontWeight.Normal,
        lineHeight   = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 13.sp,
        fontWeight   = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 11.sp,
        fontWeight   = FontWeight.Medium,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily   = LatoFamily,
        fontSize     = 10.sp,
        fontWeight   = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
)
