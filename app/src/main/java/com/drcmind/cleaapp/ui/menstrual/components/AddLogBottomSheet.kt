package com.drcmind.cleaapp.ui.menstrual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drcmind.cleaapp.data.model.FlowLevel
import com.drcmind.cleaapp.data.model.Mood
import com.drcmind.cleaapp.domain.model.Symptom
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogBottomSheet(
    symptoms: List<Symptom>,
    onDismiss: () -> Unit,
    onSave: (date: String, flow: String, pain: Int, mood: String, selectedSymptoms: List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Utilisation de SimpleDateFormat pour la compatibilité offline sans desugaring
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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Comment allez-vous ?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            SectionTitle("Flux menstruel")
            FlowSelector(selectedFlow = selectedFlow, onFlowSelected = { selectedFlow = it })

            SectionTitle("Votre humeur")
            MoodSelector(selectedMood = selectedMood, onMoodSelected = { selectedMood = it })

            SectionTitle("Niveau de douleur : ${painLevel.toInt()}")
            Slider(
                value = painLevel,
                onValueChange = { painLevel = it },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF913131),
                    activeTrackColor = Color(0xFF913131)
                )
            )

            SectionTitle("Symptômes")
            SymptomGrid(
                symptoms = symptoms,
                selectedIds = selectedSymptomIds,
                onToggleSymptom = { id ->
                    if (selectedSymptomIds.contains(id)) selectedSymptomIds.remove(id)
                    else selectedSymptomIds.add(id)
                }
            )

            Button(
                onClick = {
                    onSave(currentDate, selectedFlow.name, painLevel.toInt(), selectedMood.name, selectedSymptomIds.toList())
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF913131)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
fun FlowSelector(selectedFlow: FlowLevel, onFlowSelected: (FlowLevel) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowLevel.entries.forEach { level ->
            FilterChip(
                selected = level == selectedFlow,
                onClick = { onFlowSelected(level) },
                label = { Text(level.name) }
            )
        }
    }
}

@Composable
fun MoodSelector(selectedMood: Mood, onMoodSelected: (Mood) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Mood.entries.forEach { mood ->
            val emoji = when(mood) {
                Mood.VERY_BAD -> "😫"
                Mood.BAD -> "😔"
                Mood.NORMAL -> "😐"
                Mood.GOOD -> "🙂"
                Mood.VERY_GOOD -> "😊"
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (mood == selectedMood) Color(0xFF913131).copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { onMoodSelected(mood) }
                    .padding(8.dp)
            ) {
                Text(emoji, fontSize = 32.sp)
                Text(mood.name.lowercase(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SymptomGrid(symptoms: List<Symptom>, selectedIds: List<String>, onToggleSymptom: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        symptoms.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { symptom ->
                    val isSelected = selectedIds.contains(symptom.id)
                    Card(
                        modifier = Modifier.weight(1f).clickable { onToggleSymptom(symptom.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF913131) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            symptom.name, 
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (isSelected) Color.White else Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}
