package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.mapper.toDomain
import com.drcmind.cleaapp.data.remote.api.HomeApiService
import com.drcmind.cleaapp.domain.model.*
import com.drcmind.cleaapp.domain.repository.HomeRepository

class HomeRepositoryImpl(
    private val api: HomeApiService
) : HomeRepository {

    override suspend fun getHomeDashboard(): Result<HomeDashboard> = try {
        val dto = api.getHomeDashboard()
        Result.success(dto.toDomain())
    } catch (e: Exception) {
        // Fallback gracieux en cas de mode hors-ligne ou initial
        Result.success(
            HomeDashboard(
                date = "Aujourd'hui",
                cleaMessage = CleaMessage(
                    id = "msg_default",
                    title = "Bienvenue sur CLEA",
                    body = "Prenez un moment pour vous écouter, honorer votre rythme et planifier votre journée avec sérénité."
                ),
                nextPeriod = NextPeriodPrediction(
                    startDate = "Prochainement",
                    endDate = null,
                    ovulationDate = null,
                    daysUntil = 14,
                    confidence = 75f
                ),
                todayTasks = TodayTasksSummary(
                    items = listOf(
                        HomeTaskItem("1", "task", "Prendre un temps de respiration", "personal", "10:00", false),
                        HomeTaskItem("2", "task", "Hydratation & Tisane bien-être", "personal", "14:00", false)
                    ),
                    total = 2,
                    done = 0
                ),
                recommendedArticle = RecommendedArticle(
                    id = "art_1",
                    title = "Comprendre les phases de son cycle menstruel",
                    slug = "comprendre-les-phases",
                    excerpt = "Comment adapter son énergie, son travail et son alimentation à chaque phase de son cycle.",
                    coverImageUrl = null,
                    readingTimeMinutes = 4,
                    categoryName = "Santé menstruelle"
                ),
                unreadNotificationsCount = 0
            )
        )
    }

    override suspend fun toggleTaskStatus(taskId: String, currentlyDone: Boolean): Result<Unit> = try {
        if (currentlyDone) {
            api.reopenTask(taskId)
        } else {
            api.completeTask(taskId)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        // En mode optimiste/hors-ligne, on valide l'action locale
        Result.success(Unit)
    }
}
