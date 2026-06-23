package com.spen.placar.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta inspirada nas cores de sistema da Apple (iOS) — sóbria e elegante,
// com acentos discretos para cada equipe.

// --- Acentos das equipes (variações claro/escuro) ---------------------------
val TeamAColorDark = Color(0xFFFF9F0A)   // systemOrange (dark)
val TeamAColorLight = Color(0xFFFF9500)  // systemOrange (light)
val TeamBColorDark = Color(0xFF0A84FF)   // systemBlue (dark)
val TeamBColorLight = Color(0xFF007AFF)  // systemBlue (light)

fun teamAColor(dark: Boolean) = if (dark) TeamAColorDark else TeamAColorLight
fun teamBColor(dark: Boolean) = if (dark) TeamBColorDark else TeamBColorLight

// Compatibilidade com referências antigas.
val TeamAColor = TeamAColorDark
val TeamBColor = TeamBColorDark

// --- Tema claro (systemGroupedBackground) -----------------------------------
val LightPrimary = Color(0xFF007AFF)
val LightSecondary = Color(0xFFFF9500)
val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5E5EA)
val LightOnBackground = Color(0xFF1C1C1E)
val LightOnSurfaceVariant = Color(0xFF8E8E93)

// --- Tema escuro (OLED elegante) --------------------------------------------
val DarkPrimary = Color(0xFF0A84FF)
val DarkSecondary = Color(0xFFFF9F0A)
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF1C1C1E)         // systemGray6 (dark)
val DarkSurfaceVariant = Color(0xFF2C2C2E)  // systemGray5 (dark)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFF8E8E93) // systemGray
