package com.spen.placar.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta premium minimalista (inspirada em Apple/Stripe).

// --- Tema claro ------------------------------------------------------------
val LightBackground = Color(0xFFF7F7F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3F4F6)
val LightPrimary = Color(0xFF2563EB)
val LightOnBackground = Color(0xFF111827)
val LightOnSurfaceVariant = Color(0xFF6B7280)
val LightOutline = Color(0xFFE5E7EB)

// --- Tema escuro -----------------------------------------------------------
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkPrimary = Color(0xFF3B82F6)
val DarkOnBackground = Color(0xFFF9FAFB)
val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
val DarkOutline = Color(0xFF1F2937)

// --- Acentos das equipes ---------------------------------------------------
// Time A usa o azul primário; Time B um âmbar, garantindo leitura instantânea
// de qual placar é de cada equipe à distância.
val TeamABlueLight = Color(0xFF2563EB)
val TeamABlueDark = Color(0xFF3B82F6)
val TeamBAmberLight = Color(0xFFD97706)
val TeamBAmberDark = Color(0xFFF59E0B)

fun teamAColor(dark: Boolean) = if (dark) TeamABlueDark else TeamABlueLight
fun teamBColor(dark: Boolean) = if (dark) TeamBAmberDark else TeamBAmberLight

// Compatibilidade com referências antigas.
val TeamAColor = TeamABlueDark
val TeamBColor = TeamBAmberDark
val LightSecondary = TeamBAmberLight
val DarkSecondary = TeamBAmberDark
