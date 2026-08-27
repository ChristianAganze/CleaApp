package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MenstrualApiService(
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
        }.getOrNull() ?: "Erreur serveur (${status.value})"
        throw Exception(message)
    }

    // --- Cycles ---
    suspend fun getCycles(): List<CycleDto> {
        val response = client.get("api/cycles") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    suspend fun createCycle(startDate: String, notes: String?): CycleDto {
        val response = client.post("api/cycles") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(CreateCycleRequest(startDate = startDate, notes = notes))
        }
        return response.bodyOrThrow()
    }

    suspend fun getCycle(id: String): CycleDto {
        val response = client.get("api/cycles/$id") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    suspend fun updateCycle(id: String, params: Map<String, String>): CycleDto {
        val response = client.put("api/cycles/$id") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        return response.bodyOrThrow()
    }

    suspend fun deleteCycle(id: String): HttpResponse = client.delete("api/cycles/$id") {
        withAuth(includeCsrf = true)
    }

    suspend fun completeCycle(id: String, endDate: String, cycleLength: Int, periodLength: Int): CycleDto {
        val response = client.post("api/cycles/$id/complete") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(CompleteCycleRequest(
                endDate = endDate,
                cycleLength = cycleLength,
                periodLength = periodLength
            ))
        }
        return response.bodyOrThrow()
    }

    // --- Cycle Days ---
    suspend fun getCycleDays(cycleId: String): List<CycleDayDto> {
        val response = client.get("api/cycles/$cycleId/days") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    suspend fun addCycleDay(cycleId: String, request: CreateCycleDayRequest): CycleDayDto {
        val response = client.post("api/cycles/$cycleId/days") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    suspend fun updateCycleDay(cycleId: String, dayId: String, request: UpdateCycleDayRequest): CycleDayDto {
        val response = client.put("api/cycles/$cycleId/days/$dayId") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    suspend fun deleteCycleDay(cycleId: String, dayId: String): HttpResponse = client.delete("api/cycles/$cycleId/days/$dayId") {
        withAuth(includeCsrf = true)
    }

    suspend fun attachSymptomsToDay(cycleId: String, dayId: String, request: AttachSymptomsRequest): HttpResponse {
        return client.post("api/cycles/$cycleId/days/$dayId/symptoms") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    // --- Symptoms ---
    suspend fun getSymptoms(): List<SymptomDto> {
        val response = client.get("api/symptoms") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    // --- Predictions ---
    suspend fun getPredictions(): PredictionDto {
        val response = client.get("api/predictions") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    // --- Dashboard ---
    suspend fun getDashboard(): DashboardDto {
        val response = client.get("api/dashboard/cycle") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }
}

