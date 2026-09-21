package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.dto.ArticleDto
import com.drcmind.cleaapp.data.remote.dto.ArticleListResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ArticleApiService(
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
     * Liste des articles : GET /api/contents
     */
    suspend fun getArticles(
        category: String? = null,
        search: String? = null
    ): List<ArticleDto> {
        val response = client.get("api/contents") {
            withAuth(includeCsrf = false)
            url {
                category?.let { parameters.append("category", it) }
                search?.let { parameters.append("search", it) }
                parameters.append("per_page", "50")
            }
        }
        val listResponse = response.bodyOrThrow<ArticleListResponseDto>()
        return listResponse.data
    }

    /**
     * Détail d'un article : GET /api/contents/{id}
     */
    suspend fun getArticleDetail(id: String): ArticleDto {
        val response = client.get("api/contents/$id") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    /**
     * Basculer favori : POST /api/contents/{id}/favorite
     */
    suspend fun toggleFavorite(id: String): HttpResponse {
        return client.post("api/contents/$id/favorite") {
            withAuth(includeCsrf = true)
        }
    }
}
