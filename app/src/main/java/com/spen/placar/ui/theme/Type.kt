package com.spen.placar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografia padrão do Material 3 com um estilo extra para o placar gigante.
val Typography = Typography()

/** Estilo do número do placar — enorme e em negrito para leitura em quadra. */
val ScoreNumberStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Black,
    fontSize = 140.sp,
    letterSpacing = (-2).sp
)
