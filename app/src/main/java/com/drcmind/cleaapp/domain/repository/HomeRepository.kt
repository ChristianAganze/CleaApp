package com.drcmind.cleaapp.domain.repository

import com.drcmind.cleaapp.domain.model.HomeDashboard

interface HomeRepository {
    suspend fun getHomeDashboard(): Result<HomeDashboard>
    suspend fun toggleTaskStatus(taskId: String, currentlyDone: Boolean): Result<Unit>
}
