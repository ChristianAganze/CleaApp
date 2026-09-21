package com.drcmind.cleaapp.ui.agenda

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drcmind.cleaapp.domain.model.AgendaItemCategory
import com.drcmind.cleaapp.domain.model.AgendaKind
import com.drcmind.cleaapp.ui.components.CleaButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (
        kind: AgendaKind,
        title: String,
        category: AgendaItemCategory,
        notes: String?,
        dueAt: String?,
        remindAt: String?
    ) -> Unit,
    isSubmitting: Boolean
) {
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { dateFormat.format(Date()) }

    var selectedKind by remember { mutableStateOf(AgendaKind.TASK) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AgendaItemCategory.PERSONAL) }
    var dueDate by remember { mutableStateOf(todayDate) }
    var dueTime by remember { mutableStateOf("10:00") }
    
    var enableReminder by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf(todayDate) }
    var reminderTime by remember { mutableStateOf("09:00") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("add_agenda_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedKind == AgendaKind.TASK) "Nouvelle Tâche" else "Nouveau Rappel",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Kind Switch (Task / Reminder)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedKind == AgendaKind.TASK,
                    onClick = { selectedKind = AgendaKind.TASK },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text("Tâche avec échéance")
                }
                SegmentedButton(
                    selected = selectedKind == AgendaKind.REMINDER,
                    onClick = {
                        selectedKind = AgendaKind.REMINDER
                        enableReminder = true
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp)) }
                ) {
                    Text("Rappel programmé")
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (errorMessage != null) errorMessage = null
                },
                label = { Text("Titre de l'action *") },
                placeholder = { Text("Ex: Rendez-vous gynécologue, Séance yoga...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                isError = errorMessage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agenda_input_title")
            )

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Domaine d'organisation",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AgendaItemCategory.entries.toTypedArray()) { cat ->
                        val isSelected = selectedCategory == cat
                        val icon = when (cat) {
                            AgendaItemCategory.WORK -> Icons.Outlined.WorkOutline
                            AgendaItemCategory.FAMILY -> Icons.Outlined.FamilyRestroom
                            AgendaItemCategory.SPIRITUALITY -> Icons.Outlined.Spa
                            AgendaItemCategory.PERSONAL -> Icons.Outlined.Person
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) },
                            leadingIcon = {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Date & Time for Due Date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Date et heure prévues",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Date (AAAA-MM-JJ)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Heure (HH:MM)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }

            // Reminder Switch & Configuration
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (enableReminder || selectedKind == AgendaKind.REMINDER) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = "Notification de rappel",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Recevoir une notification push à l'heure choisie",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = enableReminder || selectedKind == AgendaKind.REMINDER,
                            onCheckedChange = {
                                if (selectedKind != AgendaKind.REMINDER) {
                                    enableReminder = it
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = enableReminder || selectedKind == AgendaKind.REMINDER) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = reminderDate,
                                onValueChange = { reminderDate = it },
                                label = { Text("Date rappel") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = reminderTime,
                                onValueChange = { reminderTime = it },
                                label = { Text("Heure") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes ou détails (optionnel)") },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Submit Button
            CleaButton(
                text = if (isSubmitting) "Enregistrement..." else "Enregistrer l'élément ✨",
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Veuillez entrer un titre pour cette action."
                        return@CleaButton
                    }
                    val dueAtFormatted = if (dueDate.isNotBlank()) "$dueDate $dueTime:00" else null
                    val remindAtFormatted = if (enableReminder || selectedKind == AgendaKind.REMINDER) {
                        "$reminderDate $reminderTime:00"
                    } else null

                    onSubmit(
                        selectedKind,
                        title.trim(),
                        selectedCategory,
                        notes.ifBlank { null },
                        dueAtFormatted,
                        remindAtFormatted
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agenda_submit_button")
            )
        }
    }
}
