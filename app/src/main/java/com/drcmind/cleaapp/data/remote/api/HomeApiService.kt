package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.dto.HomeDashboardDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class HomeApiService(
    private val client: HttpClient,
    private val authDataStore: AuthDataStore,
    private val authApi: AuthApi
) {
    private suspend fun getAuthToken(): String = authDataStore.authToken.firstOrNull() ?: ""
    private suspend fun getCsrfToken(): String = authApi.getCsrfToken()

    private suspend fun HttpRequestBuilder.withAuth(includeCsrf: Boolean = false) {
        header(HttpHeaders.Accept, "application/json")
        val token = getAuthToken()
        if (token.isNotEmpty()) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (includeCsrf) {
            val csrf = getCsrfToken()
            if (csrf.isNotEmpty() && !csrf.contains("{")) {
                header("X-XSRF-TOKEN", csrf)
            }
        }
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (status.value in 200..299) {
            return body()
        }
        val errorText = runCatching { bodyAsText() }.getOrDefault("")
        val message = runCatching {
            val json = Json.parseToJsonElement(errorText).jsonObject
            json["message"]?.jsonPrimitive?.content
        }.getOrNull() ?: "Erreur (${status.value})"
        throw Exception(message)
    }

    /**
     * Charge le tableau de bord d'accueil complet en un seul appel : GET /api/dashboard
     */
    suspend fun getHomeDashboard(): HomeDashboardDto {
        val response = client.get("api/dashboard") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    /**
     * Terminer une tâche depuis l'accueil : POST /api/agenda/items/{id}/complete
     */
    suspend fun completeTask(taskId: String): HttpResponse {
        return client.post("api/agenda/items/$taskId/complete") {
            withAuth(includeCsrf = true)
        }
    }

    /**
     * Rouvrir une tâche : POST /api/agenda/items/{id}/reopen
     */
    suspend fun reopenTask(taskId: String): HttpResponse {
        return client.post("api/agenda/items/$taskId/reopen") {
            withAuth(includeCsrf = true)
        }
    }
}
