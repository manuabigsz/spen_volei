package com.spen.placar.ui.players

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.domain.Skill
import com.spen.placar.domain.SkillLevel

/** Diálogo de cadastro/edição de um jogador e suas 5 habilidades. */
@Composable
fun PlayerEditDialog(
    initial: PlayerEntity?,
    onDismiss: () -> Unit,
    onSave: (PlayerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var saque by remember { mutableStateOf(SkillLevel.fromWeight(initial?.saque ?: 1)) }
    var recepcao by remember { mutableStateOf(SkillLevel.fromWeight(initial?.recepcao ?: 1)) }
    var levantamento by remember { mutableStateOf(SkillLevel.fromWeight(initial?.levantamento ?: 1)) }
    var corte by remember { mutableStateOf(SkillLevel.fromWeight(initial?.corte ?: 1)) }
    var movimentacao by remember { mutableStateOf(SkillLevel.fromWeight(initial?.movimentacao ?: 1)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Novo jogador" else "Editar jogador") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do atleta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                SkillSelector(Skill.SAQUE, saque) { saque = it }
                SkillSelector(Skill.RECEPCAO, recepcao) { recepcao = it }
                SkillSelector(Skill.LEVANTAMENTO, levantamento) { levantamento = it }
                SkillSelector(Skill.CORTE, corte) { corte = it }
                SkillSelector(Skill.MOVIMENTACAO, movimentacao) { movimentacao = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        (initial ?: PlayerEntity(name = "", saque = 1, recepcao = 1, levantamento = 1, corte = 1, movimentacao = 1))
                            .copy(
                                name = name.trim(),
                                saque = saque.weight,
                                recepcao = recepcao.weight,
                                levantamento = levantamento.weight,
                                corte = corte.weight,
                                movimentacao = movimentacao.weight
                            )
                    )
                }
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SkillSelector(skill: Skill, value: SkillLevel, onChange: (SkillLevel) -> Unit) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Text(skill.label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            SkillLevel.entries.forEachIndexed { index, level ->
                SegmentedButton(
                    selected = value == level,
                    onClick = { onChange(level) },
                    shape = SegmentedButtonDefaults.itemShape(index, SkillLevel.entries.size)
                ) {
                    Text(level.shortLabel(), fontSize = 13.sp)
                }
            }
        }
    }
}

private fun SkillLevel.shortLabel() = when (this) {
    SkillLevel.BASICO -> "Básico"
    SkillLevel.INTERMEDIARIO -> "Inter."
    SkillLevel.AVANCADO -> "Avanç."
}
