package com.drcmind.cleaapp.data.remote.api

import com.drcmind.cleaapp.data.remote.ApiConfig
import com.drcmind.cleaapp.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthApi(private val client: HttpClient) {

    private val baseUrl = ApiConfig.BASE_URL

    /**
     * Récupère le token CSRF.
     * Aligné strictement sur le document : GET /sanctum/csrf-cookie (SANS /api)
     */
    suspend fun getCsrfToken(): String {
        return try {
            val response = client.get("$baseUrl/sanctum/csrf-cookie") {
                header(HttpHeaders.Accept, "application/json")
            }
            
            // 1. Chercher dans les headers Set-Cookie (standard Laravel Sanctum)
            val setCookieHeaders = response.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
            var extractedToken = ""
            for (header in setCookieHeaders) {
                if (header.contains("XSRF-TOKEN=")) {
                    val raw = header.substringAfter("XSRF-TOKEN=").substringBefore(";")
                    extractedToken = runCatching { java.net.URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
                    break
                }
            }
            
            if (extractedToken.isNotEmpty()) {
                extractedToken
            } else {
                val body = response.bodyAsText().trim()
                if (body.startsWith("{")) {
                    val json = Json.parseToJsonElement(body).jsonObject
                    json["token"]?.jsonPrimitive?.content 
                        ?: json["csrf-token"]?.jsonPrimitive?.content 
                        ?: json["_token"]?.jsonPrimitive?.content 
                        ?: ""
                } else if (body.startsWith("<!DOCTYPE") || body.startsWith("<html")) {
                    ""
                } else {
                    body
                }
            }
        } catch (e: Exception) { 
            "" 
        }
    }

    suspend fun login(request: LoginRequestDto, csrfToken: String): HttpResponse {
        return client.post("$baseUrl/api/login") {
            header(HttpHeaders.Accept, "application/json")
            if (csrfToken.isNotEmpty() && !csrfToken.contains("{")) {
                header("X-XSRF-TOKEN", csrfToken)
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun register(request: RegisterRequestDto, csrfToken: String): HttpResponse {
        return client.post("$baseUrl/api/register") {
            header(HttpHeaders.Accept, "application/json")
            if (csrfToken.isNotEmpty() && !csrfToken.contains("{")) {
                header("X-XSRF-TOKEN", csrfToken)
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getUser(authToken: String): HttpResponse {
        return client.get("$baseUrl/api/user") {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.Authorization, "Bearer $authToken")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequestDto, authToken: String, csrfToken: String): HttpResponse {
        return client.patch("$baseUrl/api/user") {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.Authorization, "Bearer $authToken")
            if (csrfToken.isNotEmpty() && !csrfToken.contains("{")) {
                header("X-XSRF-TOKEN", csrfToken)
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun updatePassword(request: UpdatePasswordRequestDto, authToken: String, csrfToken: String): HttpResponse {
        return client.patch("$baseUrl/api/user/password") {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.Authorization, "Bearer $authToken")
            if (csrfToken.isNotEmpty() && !csrfToken.contains("{")) {
                header("X-XSRF-TOKEN", csrfToken)
            }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun logout(authToken: String, csrfToken: String): HttpResponse {
        return client.post("$baseUrl/api/logout") {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.Authorization, "Bearer $authToken")
            if (csrfToken.isNotEmpty() && !csrfToken.contains("{")) {
                header("X-XSRF-TOKEN", csrfToken)
            }
        }
    }
}
