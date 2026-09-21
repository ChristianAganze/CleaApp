package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.dto.AgendaItemDto
import com.drcmind.cleaapp.data.remote.dto.AgendaListResponseDto
import com.drcmind.cleaapp.data.remote.dto.CreateAgendaItemRequest
import com.drcmind.cleaapp.data.remote.dto.UpdateAgendaItemRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AgendaApiService(
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

    /**
     * Liste les items de l'agenda : GET /api/agenda/items
     */
    suspend fun getAgendaItems(
        category: String? = null,
        kind: String? = null,
        status: String? = null,
        upcoming: Int? = null
    ): List<AgendaItemDto> {
        val response = client.get("api/agenda/items") {
            withAuth(includeCsrf = false)
            url {
                category?.let { parameters.append("category", it) }
                kind?.let { parameters.append("kind", it) }
                status?.let { parameters.append("status", it) }
                upcoming?.let { parameters.append("upcoming", it.toString()) }
                parameters.append("per_page", "50")
            }
        }
        val listResponse = response.bodyOrThrow<AgendaListResponseDto>()
        return listResponse.data
    }

    /**
     * Création d'un élément d'agenda : POST /api/agenda/items
     */
    suspend fun createAgendaItem(request: CreateAgendaItemRequest): AgendaItemDto {
        val response = client.post("api/agenda/items") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    /**
     * Détail d'un élément d'agenda : GET /api/agenda/items/{id}
     */
    suspend fun getAgendaItemDetail(id: String): AgendaItemDto {
        val response = client.get("api/agenda/items/$id") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    /**
     * Mise à jour d'un élément d'agenda : PUT /api/agenda/items/{id}
     */
    suspend fun updateAgendaItem(id: String, request: UpdateAgendaItemRequest): AgendaItemDto {
        val response = client.put("api/agenda/items/$id") {
            withAuth(includeCsrf = true)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.bodyOrThrow()
    }

    /**
     * Suppression d'un élément d'agenda : DELETE /api/agenda/items/{id}
     */
    suspend fun deleteAgendaItem(id: String) {
        val response = client.delete("api/agenda/items/$id") {
            withAuth(includeCsrf = true)
        }
        if (response.status.value !in 200..299) {
            val errorText = runCatching { response.bodyAsText() }.getOrDefault("")
            throw Exception("Impossible de supprimer ($errorText)")
        }
    }

    /**
     * Terminer un élément : POST /api/agenda/items/{id}/complete
     */
    suspend fun completeItem(id: String): HttpResponse {
        return client.post("api/agenda/items/$id/complete") {
            withAuth(includeCsrf = true)
        }
    }

    /**
     * Rouvrir un élément : POST /api/agenda/items/{id}/reopen
     */
    suspend fun reopenItem(id: String): HttpResponse {
        return client.post("api/agenda/items/$id/reopen") {
            withAuth(includeCsrf = true)
        }
    }
}
