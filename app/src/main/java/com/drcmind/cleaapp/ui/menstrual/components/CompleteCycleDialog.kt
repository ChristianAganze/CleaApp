package com.drcmind.cleaapp.ui.menstrual.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drcmind.cleaapp.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CompleteCycleDialog(
    onDismiss: () -> Unit,
    onConfirm: (endDate: String, cycleLength: Int, periodLength: Int) -> Unit
) {
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var periodLength by remember { mutableStateOf("5") }
    var cycleLength by remember { mutableStateOf("28") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.complete_cycle_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.complete_cycle_message))
                
                OutlinedTextField(
                    value = periodLength,
                    onValueChange = { periodLength = it },
                    label = { Text(stringResource(R.string.complete_cycle_period_length_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = cycleLength,
                    onValueChange = { cycleLength = it },
                    label = { Text(stringResource(R.string.complete_cycle_total_length_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(currentDate, cycleLength.toIntOrNull() ?: 28, periodLength.toIntOrNull() ?: 5) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF913131))
            ) {
                Text(stringResource(R.string.complete_cycle_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

