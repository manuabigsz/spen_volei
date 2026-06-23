package com.spen.placar.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formata milissegundos como mm:ss (ou h:mm:ss se passar de 1h). */
fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))
