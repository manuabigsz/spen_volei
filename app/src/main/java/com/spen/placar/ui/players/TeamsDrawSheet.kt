package com.spen.placar.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spen.placar.data.local.PlayerConstraintEntity
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.total
import com.spen.placar.domain.BalanceMode
import com.spen.placar.domain.BalancedTeams

/** Folha inferior para configurar e visualizar o sorteio de times. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsDrawSheet(
    presentCount: Int,
    players: List<PlayerEntity>,
    constraints: List<PlayerConstraintEntity>,
    teams: BalancedTeams<PlayerEntity>?,
    unlocked: Boolean,
    onDraw: (Int, BalanceMode) -> Unit,
    onAddConstraint: (Long, Long) -> Unit,
    onRemoveConstraint: (Long) -> Unit,
    onShare: (String) -> Unit,
    onSaveCloud: () -> Unit,
    onUseInScoreboard: (List<String>, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var teamCount by remember { mutableIntStateOf(2) }
    var showLevels by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(BalanceMode.TOTAL) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Sortear times", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "$presentCount jogador(es) presente(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Seletor de número de times
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Número de times", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (teamCount > 2) teamCount-- },
                        enabled = teamCount > 2
                    ) { Icon(Icons.Filled.Remove, contentDescription = "Menos times") }
                    Text(
                        teamCount.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (teamCount < 6) teamCount++ },
                        enabled = teamCount < 6
                    ) { Icon(Icons.Filled.Add, contentDescription = "Mais times") }
                }
            }

            // Critério de equilíbrio
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Equilibrar por", fontWeight = FontWeight.Medium)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    SegmentedButton(
                        selected = mode == BalanceMode.TOTAL,
                        onClick = { mode = BalanceMode.TOTAL },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Pontos") }
                    SegmentedButton(
                        selected = mode == BalanceMode.SKILL,
                        onClick = { mode = BalanceMode.SKILL },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Habilidade") }
                }
            }

            ConstraintsSection(
                players = players,
                constraints = constraints,
                onAdd = onAddConstraint,
                onRemove = onRemoveConstraint
            )

            Button(
                onClick = { onDraw(teamCount, mode) },
                enabled = presentCount >= teamCount,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    if (teams == null) "  Sortear" else "  Sortear novamente",
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (presentCount < teamCount) {
                Text(
                    "Marque pelo menos $teamCount jogadores como presentes.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Resultado
            teams?.let { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (unlocked) {
                        IconButton(onClick = { showLevels = !showLevels }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = if (showLevels) "Ocultar níveis" else "Ver níveis",
                                tint = if (showLevels) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                result.teams.forEachIndexed { index, team ->
                    TeamCard(index = index, players = team, showLevels = showLevels && unlocked)
                }
                if (result.bench.isNotEmpty()) {
                    BenchCard(result.bench)
                }

                if (result.teams.size == 2) {
                    Button(
                        onClick = {
                            onUseInScoreboard(
                                result.teams[0].map { it.name },
                                result.teams[1].map { it.name }
                            )
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Icon(Icons.Filled.Scoreboard, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("  Usar no placar", fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onShare(buildShareText(result)) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Compartilhar")
                    }
                    FilledTonalButton(
                        onClick = onSaveCloud,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Salvar")
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamCard(index: Int, players: List<PlayerEntity>, showLevels: Boolean) {
    val level = players.sumOf { it.total }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (showLevels) {
                    Text(
                        "nível $level",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            players.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(p.name)
                    if (showLevels) {
                        Text(
                            p.total.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchCard(players: List<PlayerEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rodízio (reservas)",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                players.joinToString(", ") { it.name },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ConstraintsSection(
    players: List<PlayerEntity>,
    constraints: List<PlayerConstraintEntity>,
    onAdd: (Long, Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    val byId = remember(players) { players.associateBy { it.id } }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Restrições", fontWeight = FontWeight.Medium)
            androidx.compose.material3.TextButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Adicionar")
            }
        }

        if (constraints.isEmpty()) {
            Text(
                "Nenhuma. Ex.: marque dois jogadores que não devem cair no mesmo time.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            constraints.forEach { c ->
                val a = byId[c.aId]?.name ?: "?"
                val b = byId[c.bId]?.name ?: "?"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$a  ✕  $b", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onRemove(c.id) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remover restrição",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddConstraintDialog(
            players = players,
            onDismiss = { showAdd = false },
            onConfirm = { a, b -> onAdd(a, b); showAdd = false }
        )
    }
}

@Composable
private fun AddConstraintDialog(
    players: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var a by remember { mutableStateOf<PlayerEntity?>(null) }
    var b by remember { mutableStateOf<PlayerEntity?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Não jogam juntos") },
        text = {
            Column {
                PlayerPicker("Jogador 1", players, a) { a = it }
                Text(
                    "não pode ficar no mesmo time que",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                PlayerPicker("Jogador 2", players, b) { b = it }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                enabled = a != null && b != null && a?.id != b?.id,
                onClick = { onConfirm(a!!.id, b!!.id) }
            ) { Text("Adicionar") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun PlayerPicker(
    label: String,
    players: List<PlayerEntity>,
    selected: PlayerEntity?,
    onSelect: (PlayerEntity) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Column {
        androidx.compose.material3.OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selected?.name ?: label, modifier = Modifier.fillMaxWidth())
        }
        androidx.compose.material3.DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            players.forEach { p ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(p.name) },
                    onClick = { onSelect(p); open = false }
                )
            }
        }
    }
}

private fun buildShareText(result: BalancedTeams<PlayerEntity>): String {
    val sb = StringBuilder("🏐 Times sorteados\n\n")
    result.teams.forEachIndexed { i, team ->
        sb.append("Time ${i + 1}:\n")
        team.forEach { sb.append("  • ${it.name}\n") }
        sb.append("\n")
    }
    if (result.bench.isNotEmpty()) {
        sb.append("Rodízio: ${result.bench.joinToString(", ") { it.name }}\n")
    }
    sb.append("Liga das Nações Femininas 2029")
    return sb.toString()
}
