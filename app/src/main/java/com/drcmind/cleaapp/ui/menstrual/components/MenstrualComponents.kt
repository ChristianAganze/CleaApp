package com.drcmind.cleaapp.ui.menstrual.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
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

@Composable
fun CycleStatusCard(
    day: Int,
    totalDays: Int,
    phaseName: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val safeTotalDays = if (totalDays > 0) totalDays else 1
    val progress = (day.toFloat() / safeTotalDays).coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$day",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(R.string.dashboard_cycle_day_progress, day, safeTotalDays),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = phaseName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PredictionBanner(
    text: String,
    subText: String,
    confidence: Float? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
                confidence?.let { conf ->
                    val percentage = if (conf <= 1.0f) (conf * 100).toInt() else conf.toInt()
                    Text(
                        text = stringResource(R.string.dashboard_confidence_format, percentage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

data class PhaseAdvice(
    val title: String,
    val content: String,
    val tag: String
)

fun getAdviceForPhase(phaseName: String?, day: Int?): PhaseAdvice {
    val phaseLower = phaseName?.lowercase() ?: ""
    return when {
        phaseLower.contains("menstru") || phaseLower.contains("règle") || (day != null && day in 1..5) -> {
            PhaseAdvice(
                title = "Conseil Bien-être • Menstruation",
                content = "Boire de l'eau tiède et appliquer une douce chaleur sur le bas-ventre aide à détendre naturellement les muscles utérins et apaiser les crampes.",
                tag = "Chaleur & Décontraction"
            )
        }
        phaseLower.contains("follicul") || (day != null && day in 6..12) -> {
            PhaseAdvice(
                title = "Conseil Vitalité • Phase Folliculaire",
                content = "Vos oestrogènes remontent : c'est le moment d'intégrer des aliments riches en fer et protéines végétales pour dynamiser votre créativité et vos projets.",
                tag = "Énergie & Renouveau"
            )
        }
        phaseLower.contains("ovulat") || phaseLower.contains("fertile") || (day != null && day in 13..16) -> {
            PhaseAdvice(
                title = "Conseil Éclat • Phase Ovulatoire",
                content = "Votre pic d'énergie et de clarté mentale est optimal. Idéal pour vos rendez-vous importants, négociations et activités sportives.",
                tag = "Communication & Force"
            )
        }
        phaseLower.contains("luté") || (day != null && day >= 17) -> {
            PhaseAdvice(
                title = "Conseil Sérénité • Phase Lutéale",
                content = "La progestérone invite au ralentissement. Privilégiez les aliments riches en magnésium (amandes, chocolat noir) et un sommeil régulier.",
                tag = "Équilibre & Écoute de soi"
            )
        }
        else -> {
            PhaseAdvice(
                title = "Conseil Bien-être CLEA",
                content = "Restez à l'écoute de vos sensations corporelles et hydratez-vous régulièrement tout au long de la journée.",
                tag = "Harmonie Quotidienne"
            )
        }
    }
}

@Composable
fun HygieneTipCard(
    phaseName: String? = null,
    cycleDay: Int? = null,
    modifier: Modifier = Modifier
) {
    val advice = remember(phaseName, cycleDay) { getAdviceForPhase(phaseName, cycleDay) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = advice.title,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Text(
                    text = advice.content,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = advice.tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StartCycleCard(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.dashboard_no_active_cycle_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_no_active_cycle_desc),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.dashboard_start_cycle_button), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionSmallCard(
    title: String, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
