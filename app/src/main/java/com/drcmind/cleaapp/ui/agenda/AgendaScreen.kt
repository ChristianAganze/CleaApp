package com.drcmind.cleaapp.ui.agenda

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drcmind.cleaapp.ui.components.CleaButton

enum class AgendaCategory(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("Tous", Icons.Outlined.GridView),
    WORK("Travail", Icons.Outlined.WorkOutline),
    FAMILY("Famille", Icons.Outlined.FamilyRestroom),
    SPIRITUALITY("Spiritualité", Icons.Outlined.Spa),
    PERSONAL("Perso", Icons.Outlined.Person)
}

data class AgendaTask(
    val id: String,
    val title: String,
    val category: String,
    val time: String,
    val kind: String = "task",
    var isDone: Boolean = false,
    val hasReminder: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onNavigateToProfile: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AgendaCategory.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    val sampleTasks = remember {
        mutableStateListOf(
            AgendaTask("1", "Prière & méditation matinale", "spirituality", "07:00", isDone = true),
            AgendaTask("2", "Réunion d'équipe & suivi projet", "work", "10:30", isDone = false),
            AgendaTask("3", "Hydratation & tisane relaxante", "personal", "14:00", isDone = false),
            AgendaTask("4", "Temps de qualité en famille", "family", "19:00", isDone = false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mon Agenda",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("agenda_fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle tâche")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AgendaCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.title) },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            val filteredTasks = remember(selectedCategory, sampleTasks.toList()) {
                if (selectedCategory == AgendaCategory.ALL) {
                    sampleTasks
                } else {
                    sampleTasks.filter { it.category.equals(selectedCategory.name, ignoreCase = true) }
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Aucune tâche pour cette catégorie",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isDone) 0.dp else 1.5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val idx = sampleTasks.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        sampleTasks[idx] = sampleTasks[idx].copy(isDone = !task.isDone)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { checked ->
                                        val idx = sampleTasks.indexOfFirst { it.id == task.id }
                                        if (idx != -1) {
                                            sampleTasks[idx] = sampleTasks[idx].copy(isDone = checked)
                                        }
                                    }
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.SemiBold,
                                            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = task.time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (task.hasReminder) {
                                            Icon(
                                                imageVector = Icons.Outlined.Notifications,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newTime by remember { mutableStateOf("12:00") }
        var newCategory by remember { mutableStateOf(AgendaCategory.PERSONAL) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nouvelle tâche / rappel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Titre de la tâche") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Heure (ex: 14:30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            sampleTasks.add(
                                0,
                                AgendaTask(
                                    id = System.currentTimeMillis().toString(),
                                    title = newTitle,
                                    category = newCategory.name.lowercase(),
                                    time = newTime,
                                    isDone = false
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
