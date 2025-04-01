package com.exa.android.letstalk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
/*
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun LetsTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
// Remember System UI Controller
    val systemUiController = rememberSystemUiController()

    // Set the status bar color based on the theme
    systemUiController.setStatusBarColor(
        color = Color.White, // Use primary color for the status bar
        darkIcons = !darkTheme       // Use light or dark icons based on the theme
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}*/


// Light Color Scheme (Updated)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFC107),       // Yellow from the image
    onPrimary = Color.Black,           // Black text/icons on yellow
//    secondary = Color(0xFFBDBDBD),     // Gray for secondary elements
//    onSecondary = Color(0xFF616161),   // Darker gray text/icons
    secondary = Color.Black,
    onSecondary = Color.White,
    tertiary = Color.White,
    onTertiary = Color.Black,
    background = Color.White,          // White clean UI background
    surface = Color(0xFFF8F8F8),       // Light gray for chat backgrounds
    onSurface = Color(0xFF212121),     // Dark gray for text
    error = Color(0xFFE57373),         // Soft Red (for errors)
    primaryContainer = Color(0xFFFFC107), // Container for primary
    secondaryContainer = Color(0xFFBDBDBD) // Container for secondary
)

// Dark Color Scheme (Updated)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFC107),
    onPrimary = Color.White,
//    secondary = Color(0xFFBDBDBD),
//    onSecondary = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    tertiary = Color.Black,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF242424),
    onSurface = Color.White,
    onBackground = Color(0xFFE0E0E0),
    error = Color(0xFFEF5350),
    primaryContainer = Color(0xFFFFC107),
    secondaryContainer = Color(0xFFBDBDBD)
)

// Extended Colors
object AppColors {
    val NotificationBadge = Color(0xFFFFD700) // Gold for notification bubbles
    val DividerColor = Color(0xFFE0E0E0) // Light gray for dividers
    val cardButtonColor1 = Color.White
    val cardButtonColor2 = Color.Black
}

// Typography Setup
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = Color(0xFF333333)
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color(0xFF616161)
    )
)

// Shapes for Components
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun LetsTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }


    // Remember System UI Controller
    val systemUiController = rememberSystemUiController()

    // Set the status bar color based on the theme
    systemUiController.setStatusBarColor(
        color = Color.White, // Use primary color for the status bar
        darkIcons = !darkTheme       // Use light or dark icons based on the theme
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
