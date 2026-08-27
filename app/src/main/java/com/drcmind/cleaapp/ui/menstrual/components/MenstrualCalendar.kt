package com.drcmind.cleaapp.ui.menstrual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drcmind.cleaapp.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MenstrualCalendar(
    modifier: Modifier = Modifier,
    periodDays: List<String> = emptyList(), // Dates au format "yyyy-MM-dd"
    fertilityDays: List<String> = emptyList(),
    onMonthChange: ((Int, Int) -> Unit)? = null // year, month (1-12)
) {
    val periodDaysSet = remember(periodDays) { periodDays.toSet() }
    val fertilityDaysSet = remember(fertilityDays) { fertilityDays.toSet() }

    var displayMonth by remember { mutableStateOf(0) } // 0 = mois courant, -1 = mois précédent, etc.

    val calendar = remember { Calendar.getInstance(Locale.FRANCE) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.FRENCH) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // Calculer le mois affiché
    val displayCalendar = calendar.clone() as Calendar
    displayCalendar.add(Calendar.MONTH, displayMonth)
    displayCalendar.set(Calendar.DAY_OF_MONTH, 1)

    val monthName = monthYearFormat.format(displayCalendar.time).replaceFirstChar { it.uppercase() }
    val daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (displayCalendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0 = Lundi

    Column(modifier = modifier.fillMaxWidth()) {
        // Header avec navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { displayMonth-- }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.calendar_prev_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { displayMonth++ }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.calendar_next_month),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Notifier le changement de mois
        LaunchedEffect(displayMonth) {
            onMonthChange?.invoke(displayCalendar.get(Calendar.YEAR), displayCalendar.get(Calendar.MONTH) + 1)
        }

        // En-tête des jours
        val dayHeaders = listOf(
            stringResource(R.string.day_mon),
            stringResource(R.string.day_tue),
            stringResource(R.string.day_wed),
            stringResource(R.string.day_thu),
            stringResource(R.string.day_fri),
            stringResource(R.string.day_sat),
            stringResource(R.string.day_sun)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var dayCounter = 1
        for (week in 0..5) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        displayCalendar.set(Calendar.DAY_OF_MONTH, dayCounter)
                        val dateStr = dateFormat.format(displayCalendar.time)

                        DayItem(
                            day = dayCounter,
                            isPeriod = periodDaysSet.contains(dateStr),
                            isFertile = fertilityDaysSet.contains(dateStr),
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
            if (dayCounter > daysInMonth) break
        }
    }
}

@Composable
fun DayItem(
    day: Int,
    isPeriod: Boolean,
    isFertile: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.aspectRatio(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isPeriod) FontWeight.Bold else FontWeight.Normal
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            if (isPeriod) Box(modifier = Modifier.size(6.dp).background(Color(0xFF913131), CircleShape))
            if (isFertile) Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFD54F), CircleShape))
        }
    }
}