package com.pulse.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Pc {
    val Background = Color(0xFF090B0B)
    val Surface = Color(0xFF171C1A)
    val SurfaceElevated = Color(0xFF202724)
    val TextPrimary = Color(0xFFF0F2EF)
    val TextSecondary = Color(0xFF929C96)
    val Accent = Color(0xFF75CDB0)
    val Border = Color(0xFF29312E)
    val Error = Color(0xFFD98282)
    val Warning = Color(0xFFD9AD70)
    val OnAccent = Color(0xFF090B0B)
}

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
)

object Tx {
    val Display = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 58.sp, letterSpacing = (-1.5).sp, color = Pc.TextPrimary)
    val H1 = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp, color = Pc.TextPrimary)
    val H2 = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp, color = Pc.TextPrimary)
    val H3 = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp, color = Pc.TextPrimary)
    val Num = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp, color = Pc.TextPrimary)
    val NumSmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 22.sp, color = Pc.TextPrimary)
    val Timer = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 88.sp, lineHeight = 90.sp, letterSpacing = (-3).sp, color = Pc.TextPrimary)
    val Body = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, color = Pc.TextPrimary)
    val BodySecondary = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = Pc.TextSecondary)
    val Label = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, color = Pc.TextSecondary)
    val Small = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, color = Pc.TextSecondary)
    val Button = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp)
    val Nav = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
}

@Composable
fun PulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pc.Accent,
            onPrimary = Pc.OnAccent,
            secondary = Pc.Accent,
            onSecondary = Pc.OnAccent,
            background = Pc.Background,
            onBackground = Pc.TextPrimary,
            surface = Pc.Surface,
            onSurface = Pc.TextPrimary,
            surfaceVariant = Pc.SurfaceElevated,
            onSurfaceVariant = Pc.TextSecondary,
            outline = Pc.Border,
            error = Pc.Error,
        ),
        typography = Typography(
            bodyLarge = Tx.Body,
            bodyMedium = Tx.Body,
            bodySmall = Tx.Small,
            labelLarge = Tx.Button,
            labelMedium = Tx.Small,
            titleLarge = Tx.H2,
            titleMedium = Tx.H3,
            headlineSmall = Tx.H2,
            displayLarge = Tx.Display,
        ),
        content = content,
    )
}
