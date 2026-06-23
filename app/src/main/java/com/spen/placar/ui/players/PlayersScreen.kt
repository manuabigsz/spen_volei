package com.spen.placar.ui.players

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spen.placar.data.local.PlayerEntity
import com.spen.placar.data.local.levels
import com.spen.placar.data.local.total
import com.spen.placar.domain.Skill
import com.spen.placar.domain.SkillLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel,
    onBack: () -> Unit
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val constraints by viewModel.constraints.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var editTarget by remember { mutableStateOf<PlayerEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showDraw by remember { mutableStateOf(false) }
    var showBulkDelete by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (text != null) {
                viewModel.importCsv(text) { count ->
                    Toast.makeText(context, "Importados $count jogador(es)", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Não foi possível ler o arquivo", Toast.LENGTH_LONG).show()
            }
        }
    }

    val presentCount = players.count { it.present }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Jogadores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (presentCount > 0) {
                        IconButton(onClick = { showBulkDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remover selecionados",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Importar CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editTarget = null; showEditor = true },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text("Jogador") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (players.isEmpty()) {
                EmptyState(
                    onImport = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Cabeçalho de seleção
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$presentCount de ${players.size} selecionado(s)",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        androidx.compose.material3.TextButton(onClick = { viewModel.setAllPresent(true) }) {
                            Text("Todos")
                        }
                        androidx.compose.material3.TextButton(onClick = { viewModel.setAllPresent(false) }) {
                            Text("Nenhum")
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(players, key = { it.id }) { player ->
                        PlayerRow(
                            player = player,
                            onTogglePresent = { viewModel.togglePresent(player) },
                            onEdit = { editTarget = player; showEditor = true },
                            onDelete = { viewModel.delete(player.id) }
                        )
                    }
                }

                // Barra de sorteio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Button(
                        onClick = { showDraw = true },
                        enabled = presentCount >= 2,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("  Sortear times ($presentCount)", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showEditor) {
        PlayerEditDialog(
            initial = editTarget,
            onDismiss = { showEditor = false },
            onSave = { viewModel.upsert(it); showEditor = false }
        )
    }

    if (showDraw) {
        TeamsDrawSheet(
            presentCount = presentCount,
            players = players,
            constraints = constraints,
            teams = teams,
            onDraw = { viewModel.draw(it) },
            onAddConstraint = { a, b -> viewModel.addConstraint(a, b) },
            onRemoveConstraint = { viewModel.removeConstraint(it) },
            onShare = { shareText(context, it) },
            onDismiss = { showDraw = false; viewModel.clearDraw() }
        )
    }

    if (showBulkDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBulkDelete = false },
            title = { Text("Remover selecionados") },
            text = { Text("Excluir os $presentCount jogador(es) selecionado(s)? Esta ação não pode ser desfeita.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteSelected()
                    showBulkDelete = false
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBulkDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PlayerRow(
    player: PlayerEntity,
    onTogglePresent: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = player.present, onCheckedChange = { onTogglePresent() })
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "  ·  ${player.total}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                SkillChips(player)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SkillChips(player: PlayerEntity) {
    val levels = player.levels
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Skill.entries.forEachIndexed { i, skill ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(levelColor(levels[i]))
                )
                Text(
                    skill.short,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun levelColor(level: SkillLevel): Color = when (level) {
    SkillLevel.AVANCADO -> Color(0xFF22C55E)
    SkillLevel.INTERMEDIARIO -> Color(0xFFF59E0B)
    SkillLevel.BASICO -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
}

@Composable
private fun EmptyState(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Nenhum jogador cadastrado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Use o + para adicionar, ou importe sua planilha CSV\n(colunas: Jogador, Saque, Recepção, Levantamento, Corte, Movimentação).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            androidx.compose.material3.FilledTonalButton(
                onClick = onImport,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Importar CSV")
            }
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar"))
}
