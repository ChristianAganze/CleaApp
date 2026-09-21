package com.drcmind.cleaapp.ui.menstrual.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drcmind.cleaapp.R
import com.drcmind.cleaapp.data.model.FlowLevel
import com.drcmind.cleaapp.data.model.Mood
import com.drcmind.cleaapp.domain.model.DEFAULT_SYMPTOMS
import com.drcmind.cleaapp.domain.model.Symptom
import com.drcmind.cleaapp.ui.components.CleaButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddLogBottomSheet(
    symptoms: List<Symptom>,
    onDismiss: () -> Unit,
    onSave: (date: String, flow: String, pain: Int, mood: String, selectedSymptoms: List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val currentDate = remember { 
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) 
    }
    
    var selectedFlow by remember { mutableStateOf(FlowLevel.MEDIUM) }
    var selectedMood by remember { mutableStateOf(Mood.NORMAL) }
    var painLevel by remember { mutableFloatStateOf(0f) }
    val selectedSymptomIds = remember { mutableStateListOf<String>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_log_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.log_sheet_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Journal du $currentDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 1. Flux Menstruel (Material 3 FlowRow with FilterChips)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(stringResource(R.string.log_flow_section))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowLevel.entries.forEach { level ->
                        val isSelected = level == selectedFlow
                        val label = when (level) {
                            FlowLevel.NONE -> stringResource(R.string.flow_none)
                            FlowLevel.SPOTTING -> stringResource(R.string.flow_spotting)
                            FlowLevel.LIGHT -> stringResource(R.string.flow_light)
                            FlowLevel.MEDIUM -> stringResource(R.string.flow_medium)
                            FlowLevel.HEAVY -> stringResource(R.string.flow_heavy)
                        }
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFlow = level },
                            label = { Text(label) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 2. Humeur du Jour
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(stringResource(R.string.log_mood_section))
                MoodSelector(selectedMood = selectedMood, onMoodSelected = { selectedMood = it })
            }

            // 3. Niveau d'inconfort / Douleur
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("Intensité de la douleur")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (painLevel > 6f) {
                            MaterialTheme.colorScheme.errorContainer
                        } else if (painLevel > 3f) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = "${painLevel.toInt()} / 10",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Slider(
                    value = painLevel,
                    onValueChange = { painLevel = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // 4. Symptômes ressentis (Material 3 FlowRow with FilterChips)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(stringResource(R.string.log_symptoms_section))
                
                val displaySymptoms = if (symptoms.isNotEmpty()) symptoms else DEFAULT_SYMPTOMS
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displaySymptoms.forEach { symptom ->
                        val isSelected = selectedSymptomIds.contains(symptom.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedSymptomIds.remove(symptom.id)
                                else selectedSymptomIds.add(symptom.id)
                            },
                            label = { Text(symptom.name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Submit Button
            CleaButton(
                text = stringResource(R.string.log_submit_button),
                onClick = {
                    onSave(
                        currentDate,
                        selectedFlow.name,
                        painLevel.toInt(),
                        selectedMood.name,
                        selectedSymptomIds.toList()
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("submit_log_button")
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun MoodSelector(selectedMood: Mood, onMoodSelected: (Mood) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Mood.entries.forEach { mood ->
            val (emoji, labelRes) = when(mood) {
                Mood.VERY_BAD -> "😫" to R.string.mood_very_bad
                Mood.BAD -> "😔" to R.string.mood_bad
                Mood.NORMAL -> "😐" to R.string.mood_normal
                Mood.GOOD -> "🙂" to R.string.mood_good
                Mood.VERY_GOOD -> "😊" to R.string.mood_very_good
            }
            val label = stringResource(labelRes)
            val isSelected = mood == selectedMood
            
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = if (isSelected) 3.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp)
                    .clickable { onMoodSelected(mood) }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
