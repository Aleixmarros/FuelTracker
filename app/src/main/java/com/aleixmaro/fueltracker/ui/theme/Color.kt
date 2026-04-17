package com.aleixmaro.fueltracker.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)
fun getConsumptionColor(consumption: Double): Color {
    return when {
        consumption < 6.0 -> Color(0xFF00FF00)         // Verde chillón
        consumption in 6.0..6.99 -> Color(0xFF00AA00) // Verde
        consumption in 7.0..7.99 -> Color(0xFF007700) // Verde oscuro
        consumption in 8.0..8.99 -> Color(0xFFCCC000) // Amarillo apagado
        consumption in 9.0..9.99 -> Color(0xFFFFFF00) // Amarillo chillón
        consumption in 10.0..10.99 -> Color(0xFFFF8800) // Naranja
        consumption in 11.0..11.99 -> Color(0xFFFF4400)// Naranja chillón
        else -> Color(0xFFFF0000)                      // Rojo
    }
}