package com.spen.placar.ui.scoreboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spen.placar.data.prefs.AppSettings
import com.spen.placar.data.prefs.ThemeMode

/** Diálogo para renomear uma equipe. */
@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nome da equipe") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Diálogo de configurações: tema, som, vibração e S Pen. */
@Composable
fun SettingsDialog(
    settings: AppSettings,
    spenAvailable: Boolean,
    onTheme: (ThemeMode) -> Unit,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onSpen: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações") },
        text = {
            Column {
                Text("Tema", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = settings.themeMode == mode,
                                onClick = { onTheme(mode) }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.themeMode == mode, onClick = { onTheme(mode) })
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "Sistema"
                                ThemeMode.LIGHT -> "Claro"
                                ThemeMode.DARK -> "Escuro"
                            }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                ToggleRow("Sons", settings.soundEnabled, onSound)
                ToggleRow("Vibração", settings.vibrationEnabled, onVibration)
                ToggleRow(
                    label = if (spenAvailable) "Controle por S Pen (conectada)"
                    else "Controle por S Pen",
                    checked = settings.spenEnabled,
                    onCheckedChange = onSpen
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = checked, onClick = { onCheckedChange(!checked) }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.padding(vertical = 8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
