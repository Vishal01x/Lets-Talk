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
    primary = Color(0xFFFDB92F),       // Golden yellow - exact from screenshot
    onPrimary = Color.Black,           // Black text/icons on yellow
    secondary = Color.Black,           // Black for icons and secondary elements
    onSecondary = Color.White,         // White text on black
    tertiary = Color.White,            // White for surfaces
    onTertiary = Color.Black,          // Black text on white
    background = Color.White,          // White background
    onBackground = Color.Black,        // Black text on background
    surface = Color(0xFFF5F5F5),       // Very light gray for chat background
    onSurface = Color.Black,           // Black text on surface
    surfaceVariant = Color(0xFFF0F0F0), // Light gray for received messages
    onSurfaceVariant = Color(0xFF666666), // Gray text for placeholders
    error = Color(0xFFE57373),         // Soft red for errors
    primaryContainer = Color(0xFFFFC107), // Yellow container
    secondaryContainer = Color(0xFFE0E0E0), // Light gray container
    outline = Color(0xFFE0E0E0)        // Light gray for borders/dividers
)

// Dark Color Scheme (Updated)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFDB92F),       // Golden yellow - same as light mode
    onPrimary = Color.Black,           // Black text on yellow (matches light mode)
    secondary = Color.White,           // White for icons and secondary elements
    onSecondary = Color.Black,         // Black text on white
    tertiary = Color(0xFF1C1C1E),      // Dark for surfaces
    onTertiary = Color.White,          // White text on dark
    background = Color(0xFF1C1C1E),    // Dark background
    onBackground = Color(0xFFE0E0E0),  // Light gray text on background
    surface = Color(0xFF2C2C2E),       // Medium dark gray for elevated surfaces
    onSurface = Color.White,           // White text on surface
    surfaceVariant = Color(0xFF3A3A3C), // Medium gray for received messages
    onSurfaceVariant = Color(0xFF999999), // Gray text for placeholders
    error = Color(0xFFEF5350),         // Red for errors
    primaryContainer = Color(0xFFFFC107), // Yellow container
    secondaryContainer = Color(0xFF3A3A3C), // Dark gray container
    outline = Color(0xFF48484A)        // Gray for borders/dividers
)

// Extended Colors
object AppColors {
    // Message Bubbles
    val messageSentBackground = Color(0xFFFFC107)        // Yellow for sent messages
    val messageReceivedBackgroundLight = Color(0xFFF0F0F0) // Light gray for received (light mode)
    val messageReceivedBackgroundDark = Color(0xFF3A3A3C)  // Dark gray for received (dark mode)
    val messageTextOnYellow = Color.Black                 // Black text on yellow bubbles
    
    // Status Indicators
    val onlineIndicator = Color(0xFF4CAF50)              // Green for online status
    val unreadBadge = Color(0xFFFFC107)                  // Yellow for unread count badge
    val checkmarkDelivered = Color(0xFF666666)           // Gray for delivered status
    val checkmarkRead = Color(0xFF4CAF50)                // Green for read status
    
    // UI Elements
    val dividerColor = Color(0xFFE0E0E0)                 // Light gray for dividers
    val dividerColorDark = Color(0xFF48484A)             // Dark gray for dividers (dark mode)
    val cardButtonColor1 = Color.White
    val cardButtonColor2 = Color.Black
    val storyBorderGradientStart = Color(0xFFFFC107)     // Yellow gradient start for stories
    val storyBorderGradientEnd = Color(0xFFFF9800)       // Orange gradient end for stories
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
    small = RoundedCornerShape(8.dp),    // For small elements like badges
    medium = RoundedCornerShape(12.dp),   // For buttons and cards
    large = RoundedCornerShape(20.dp)     // For message bubbles
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
