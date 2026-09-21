package com.drcmind.cleaapp.data.mapper

import com.drcmind.cleaapp.data.remote.dto.*
import com.drcmind.cleaapp.domain.model.*

fun HomeDashboardDto.toDomain(): HomeDashboard {
    return HomeDashboard(
        date = date ?: "",
        cleaMessage = cleaMessage?.let {
            CleaMessage(
                id = it.id ?: "",
                title = it.title ?: "Douceur & Énergie",
                body = it.body
            )
        },
        nextPeriod = nextPeriod?.let {
            NextPeriodPrediction(
                startDate = it.predictedPeriodStart ?: "",
                endDate = it.predictedPeriodEnd,
                ovulationDate = it.predictedOvulation,
                daysUntil = it.daysUntil ?: 0,
                confidence = it.confidence ?: 0f
            )
        },
        todayTasks = todayTasks?.let {
            TodayTasksSummary(
                items = it.items.map { item ->
                    HomeTaskItem(
                        id = item.id,
                        kind = item.kind,
                        title = item.title,
                        category = item.category ?: "personal",
                        dueAt = item.dueAt,
                        isDone = item.status.equals("done", ignoreCase = true)
                    )
                },
                total = it.total,
                done = it.done
            )
        } ?: TodayTasksSummary(emptyList(), 0, 0),
        recommendedArticle = recommendedArticle?.let {
            RecommendedArticle(
                id = it.id,
                title = it.title,
                slug = it.slug ?: "",
                excerpt = it.excerpt ?: "",
                coverImageUrl = it.coverImageUrl,
                readingTimeMinutes = it.readingTimeMinutes,
                categoryName = it.category?.name ?: "Bien-être"
            )
        },
        unreadNotificationsCount = unreadNotificationsCount
    )
}
