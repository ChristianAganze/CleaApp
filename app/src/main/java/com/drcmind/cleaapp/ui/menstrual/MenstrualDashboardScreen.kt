package com.drcmind.cleaapp.ui.menstrual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drcmind.cleaapp.R
import com.drcmind.cleaapp.domain.model.*
import com.drcmind.cleaapp.ui.components.CleaLogo
import com.drcmind.cleaapp.ui.menstrual.components.ActionSmallCard
import com.drcmind.cleaapp.ui.menstrual.components.AddLogBottomSheet
import com.drcmind.cleaapp.ui.menstrual.components.CompleteCycleDialog
import com.drcmind.cleaapp.ui.menstrual.components.CycleStatusCard
import com.drcmind.cleaapp.ui.menstrual.components.HygieneTipCard
import com.drcmind.cleaapp.ui.menstrual.components.MenstrualCalendar
import com.drcmind.cleaapp.ui.menstrual.components.PredictionBanner
import com.drcmind.cleaapp.ui.menstrual.components.StartCycleCard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenstrualDashboardScreen(
    viewModel: MenstrualViewModel = koinViewModel(),
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAddLogSheet by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val dashboard = state.dashboard

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.brand_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    CleaLogo(
                        horizontal = true,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.dashboard_notifications_cd))
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        state.user?.name?.let { name ->
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        } ?: Icon(Icons.Default.Person, contentDescription = stringResource(R.string.dashboard_profile_cd))
                    }
                }
            )
        },
        floatingActionButton = {
            val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            val todayLogged = dashboard?.activeCycle?.cycleDays?.any { it.date == today } ?: false
            
            if (dashboard?.activeCycle != null && !todayLogged) {
                ExtendedFloatingActionButton(
                    onClick = { showAddLogSheet = true },
                    containerColor = Color(0xFFAD5C5C),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.dashboard_log_day_fab)) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            if (state.isLoading && dashboard == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF913131))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = if (dashboard?.activeCycle != null) 96.dp else 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        state.user?.let { user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.dashboard_greeting, user.name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val currentDay = dashboard?.activeCycle?.getCurrentDay()
                        val phase = dashboard?.getPhaseName()
                        HygieneTipCard(
                            phaseName = phase,
                            cycleDay = currentDay
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.dashboard_my_cycle),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (dashboard?.activeCycle == null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StartCycleCard(onStart = { viewModel.startNewCycle() })
                        }
                    } else {
                        item {
                            val currentDay = dashboard.activeCycle.getCurrentDay()
                            val totalDays = dashboard.stats.averageCycleLength
                            val phase = dashboard.getPhaseName()
                            val desc = dashboard.getPhaseDescription()
                            
                            CycleStatusCard(
                                day = currentDay,
                                totalDays = totalDays,
                                phaseName = phase,
                                description = desc
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                dashboard.predictions?.let { pred ->
                                    PredictionBanner(
                                        text = stringResource(R.string.dashboard_prediction_period, pred.predictedPeriodStart),
                                        subText = stringResource(R.string.dashboard_prediction_fertility),
                                        confidence = pred.confidence
                                    )
                                }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    MenstrualCalendar(
                                        modifier = Modifier.padding(16.dp),
                                        periodDays = dashboard.activeCycle.cycleDays.map { it.date },
                                        fertilityDays = dashboard.predictions?.getFertilityRange() ?: emptyList()
                                    )
                                }

                                Button(
                                    onClick = { showCompleteDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.dashboard_complete_cycle))
                                }
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActionSmallCard(
                                title = stringResource(R.string.dashboard_action_symptoms),
                                onClick = { 
                                    showAddLogSheet = true 
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ActionSmallCard(
                                title = stringResource(R.string.dashboard_action_analytics),
                                onClick = { /* Bientôt disponible */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (state.isLoading && dashboard != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter), color = Color(0xFF913131))
            }
            
            // Affichage des erreurs ou succès éventuels
            LaunchedEffect(state.error) {
                state.error?.let {
                    snackbarHostState.showSnackbar(it)
                }
            }

            val logSuccessMessage = stringResource(R.string.dashboard_log_success)
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    snackbarHostState.showSnackbar(logSuccessMessage)
                    viewModel.resetSuccess()
                }
            }
        }

        if (showAddLogSheet) {
            AddLogBottomSheet(
                symptoms = state.symptoms,
                onDismiss = { showAddLogSheet = false },
                onSave = { date, flow, pain, mood, selectedSymptoms ->
                    val cycleId = dashboard?.activeCycle?.id ?: ""
                    viewModel.addDailyLog(cycleId, date, flow, pain, mood, selectedSymptoms)
                    showAddLogSheet = false
                }
            )
        }

        if (showCompleteDialog) {
            CompleteCycleDialog(
                onDismiss = { showCompleteDialog = false },
                onConfirm = { endDate, cycleLen, periodLen ->
                    dashboard?.activeCycle?.id?.let { id ->
                        viewModel.completeCycle(id, endDate, cycleLen, periodLen)
                    }
                    showCompleteDialog = false
                }
            )
        }
    }
}
