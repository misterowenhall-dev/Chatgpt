package com.pianopath.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF182033)
val Purple = Color(0xFF6750E8)
val PurpleDark = Color(0xFF4933BC)
val PurpleSoft = Color(0xFFEDE9FF)
val Green = Color(0xFF2CCB91)
val GreenSoft = Color(0xFFE2FAF1)
val Red = Color(0xFFEF675F)
val RedSoft = Color(0xFFFFE9E7)
val Gold = Color(0xFFF6B73C)
val Blue = Color(0xFF4587F7)
val Paper = Color(0xFFF7F7FC)
val CardWhite = Color.White
val Muted = Color(0xFF737B91)
val Line = Color(0xFFE4E6EE)
val Locked = Color(0xFFC8CCD8)

@Composable
fun PianoPathTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Purple,
            onPrimary = Color.White,
            primaryContainer = PurpleSoft,
            onPrimaryContainer = Ink,
            secondary = Green,
            background = Paper,
            surface = CardWhite,
            onSurface = Ink,
            outline = Line,
            error = Red
        ),
        content = content
    )
}
