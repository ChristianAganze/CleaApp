package com.drcmind.cleaapp.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class MenstrualCycle(
    val id: String,
    val startDate: String,
    val endDate: String?,
    val cycleLength: Int?,
    val periodLength: Int?,
    val status: String,
    val notes: String?,
    val cycleDays: List<CycleDay> = emptyList()
)

fun MenstrualCycle.getCurrentDay(): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = sdf.parse(startDate) ?: return 1
        val today = Date()
        val diff = today.time - start.time
        ((diff / (1000 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
    } catch (e: Exception) { 1 }
}

data class CycleDay(
    val id: String,
    val date: String,
    val flow: String,
    val painLevel: Int?,
    val mood: String?,
    val temperature: Float?,
    val weight: Float?,
    val medications: String?,
    val notes: String?,
    val symptoms: List<Symptom> = emptyList()
)

data class Symptom(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val icon: String?
)

val DEFAULT_SYMPTOMS = listOf(
    Symptom(id = "1", name = "Crampes", slug = "crampes", description = "Douleurs abdominales ou crampes utérines", icon = "cramps"),
    Symptom(id = "2", name = "Maux de tête", slug = "headache", description = "Migraines ou céphalées", icon = "headache"),
    Symptom(id = "3", name = "Fatigue", slug = "fatigue", description = "Baisse d'énergie et somnolence", icon = "fatigue"),
    Symptom(id = "4", name = "Ballonnements", slug = "bloating", description = "Gonflement abdominal", icon = "bloating"),
    Symptom(id = "5", name = "Acné", slug = "acne", description = "Éruptions cutanées", icon = "acne"),
    Symptom(id = "6", name = "Sautes d'humeur", slug = "mood_swings", description = "Sensibilité émotionnelle accrue", icon = "mood"),
    Symptom(id = "7", name = "Sensibilité mammaire", slug = "breast_tenderness", description = "Tensions dans la poitrine", icon = "breast"),
    Symptom(id = "8", name = "Douleurs lombaires", slug = "backache", description = "Douleurs dans le bas du dos", icon = "backache"),
    Symptom(id = "9", name = "Insomnie", slug = "insomnia", description = "Difficultés de sommeil", icon = "sleep"),
    Symptom(id = "10", name = "Nausées", slug = "nausea", description = "Inconfort gastrique", icon = "nausea")
)

data class Prediction(
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val cycleVariation: Int,
    val predictedPeriodStart: String,
    val predictedPeriodEnd: String,
    val predictedOvulation: String,
    val fertilityStart: String,
    val fertilityEnd: String,
    val confidence: Float
)

fun Prediction.getFertilityRange(): List<String> {
    val dates = mutableListOf<String>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
        val start = sdf.parse(fertilityStart)
        val end = sdf.parse(fertilityEnd)
        if (start != null && end != null) {
            val cal = Calendar.getInstance()
            cal.time = start
            while (!cal.time.after(end)) {
                dates.add(sdf.format(cal.time))
                cal.add(Calendar.DATE, 1)
            }
        }
    } catch (e: Exception) {}
    return dates
}

data class MenstrualDashboard(
    val activeCycle: MenstrualCycle?,
    val predictions: Prediction?,
    val stats: MenstrualStats
)

fun MenstrualDashboard.getPhaseName(): String {
    val day = activeCycle?.getCurrentDay() ?: return "Analyse en cours"
    val periodLength = stats.averagePeriodLength
    val ovulationDay = predictions?.predictedOvulation?.let { predOvulation ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = sdf.parse(activeCycle?.startDate ?: "") ?: return "Phase Folliculaire"
            val ovulation = sdf.parse(predOvulation)
            if (ovulation != null) {
                val diff = (ovulation.time - start.time) / (1000 * 60 * 60 * 24)
                (diff + 1).toInt().coerceAtLeast(periodLength + 1)
            } else 14
        } catch (e: Exception) { 14 }
    } ?: 14
    return when {
        day <= periodLength -> "Phase Menstruelle"
        day < ovulationDay -> "Phase Folliculaire"
        day == ovulationDay -> "Phase d'Ovulation"
        else -> "Phase Lutéale"
    }
}

fun MenstrualDashboard.getPhaseDescription(): String {
    return when (getPhaseName()) {
        "Phase Menstruelle" -> "Période de repos et d'écoute de votre corps. Privilégiez les boissons chaudes et le calme."
        "Phase Folliculaire" -> "Regain d'énergie et de créativité. Moment idéal pour planifier et démarrer des projets."
        "Phase d'Ovulation" -> "Votre énergie et votre fertilité sont au sommet. Moment clé de votre cycle."
        "Phase Lutéale" -> "Ralentissement progressif. Prenez soin de vous et favorisez un sommeil réparateur."
        else -> "Suivi et analyse de votre cycle en cours."
    }
}

data class MenstrualStats(
    val completedCyclesCount: Int,
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val cycleVariation: Int
)
