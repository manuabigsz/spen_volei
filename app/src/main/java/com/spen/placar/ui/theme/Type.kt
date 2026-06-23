package com.spen.placar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografia refinada (estilo Apple): pesos leves para textos, peso médio para
// títulos. Aproxima o SF Pro usando a fonte sans-serif do sistema.

private val sans = FontFamily.SansSerif

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 17.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 15.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 0.4.sp
    )
)

/** Nome da equipe — maiúsculas espaçadas, leve e discreto. */
val TeamNameStyle = TextStyle(
    fontFamily = sans,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    letterSpacing = 3.sp
)
