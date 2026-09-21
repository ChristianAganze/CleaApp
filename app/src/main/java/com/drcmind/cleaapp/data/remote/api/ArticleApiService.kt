package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.dto.ArticleDto
import com.drcmind.cleaapp.data.remote.dto.ArticleListResponseDto
import com.drcmind.cleaapp.data.remote.dto.CategoryDto
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
     * Catégories : GET /api/content/categories
     */
    suspend fun getCategories(): List<CategoryDto> {
        return try {
            val response = client.get("api/content/categories") {
                withAuth(includeCsrf = false)
            }
            response.bodyOrThrow()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Liste des articles publiés : GET /api/content/articles
     * Paramètres :
     *  - category : slug de catégorie (ex. "couple", "sante-menstruelle")
     *  - q : recherche plein texte
     *  - favorites_only : 1 si favoris uniquement
     *  - sort : "latest" (défaut), "popular", "reading_time"
     *  - per_page : 1-50 (défaut 50)
     */
    suspend fun getArticles(
        category: String? = null,
        q: String? = null,
        favoritesOnly: Boolean = false,
        sort: String? = "latest",
        perPage: Int = 50
    ): List<ArticleDto> {
        val response = client.get("api/content/articles") {
            withAuth(includeCsrf = false)
            url {
                category?.takeIf { it.isNotBlank() }?.let { parameters.append("category", it) }
                q?.takeIf { it.isNotBlank() }?.let { parameters.append("q", it) }
                if (favoritesOnly) {
                    parameters.append("favorites_only", "1")
                }
                sort?.let { parameters.append("sort", it) }
                parameters.append("per_page", perPage.coerceIn(1, 50).toString())
            }
        }
        val listResponse = response.bodyOrThrow<ArticleListResponseDto>()
        return listResponse.data
    }

    /**
     * Détail d'un article : GET /api/content/articles/{slug}
     */
    suspend fun getArticleDetail(slug: String): ArticleDto {
        val response = client.get("api/content/articles/$slug") {
            withAuth(includeCsrf = false)
        }
        return response.bodyOrThrow()
    }

    /**
     * Ajouter aux favoris : POST /api/content/articles/{slug}/favorite
     */
    suspend fun addFavorite(slug: String): HttpResponse {
        return client.post("api/content/articles/$slug/favorite") {
            withAuth(includeCsrf = true)
        }
    }

    /**
     * Retirer des favoris : DELETE /api/content/articles/{slug}/favorite
     */
    suspend fun removeFavorite(slug: String): HttpResponse {
        return client.delete("api/content/articles/$slug/favorite") {
            withAuth(includeCsrf = true)
        }
    }

    /**
     * Mes favoris paginés : GET /api/content/articles/favorites
     */
    suspend fun getFavoriteArticles(): List<ArticleDto> {
        return try {
            val response = client.get("api/content/articles/favorites") {
                withAuth(includeCsrf = false)
                url {
                    parameters.append("per_page", "50")
                }
            }
            val listResponse = response.bodyOrThrow<ArticleListResponseDto>()
            listResponse.data
        } catch (_: Exception) {
            getArticles(favoritesOnly = true)
        }
    }
}
