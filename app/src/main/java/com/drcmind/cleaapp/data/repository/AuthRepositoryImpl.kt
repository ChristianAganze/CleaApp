package com.drcmind.cleaapp.data.repository

import com.drcmind.cleaapp.data.local.datastore.AuthDataStore
import com.drcmind.cleaapp.data.remote.api.AuthApi
import com.drcmind.cleaapp.data.remote.dto.*
import com.drcmind.cleaapp.domain.model.AuthResult
import com.drcmind.cleaapp.domain.model.User
import com.drcmind.cleaapp.domain.repository.AuthRepository
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dataStore: AuthDataStore
) : AuthRepository {

    override suspend fun getCsrfToken(): String = api.getCsrfToken()

    override suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            val csrfToken = api.getCsrfToken()
            val response = api.login(LoginRequestDto(email, password), csrfToken)
            
            if (response.status == HttpStatusCode.OK) {
                val authResponse = response.body<AuthResponseDto>()
                if (authResponse.accessToken != null) {
                    dataStore.saveToken(authResponse.accessToken)
                    authResponse.user?.let { dataStore.saveUser(it.name, it.email) }
                    AuthResult.Success(Unit)
                } else {
                    AuthResult.Error("Échec : Jeton d'accès manquant.")
                }
            } else {
                handleResponseError(response)
            }
        } catch (e: Exception) {
            AuthResult.Error("Erreur de connexion : ${e.message}")
        }
    }

    override suspend fun register(name: String, email: String, password: String, passwordConfirmation: String): AuthResult<Unit> {
        return try {
            val csrfToken = api.getCsrfToken()
            val response = api.register(RegisterRequestDto(name = name, email = email, password = password, passwordConfirmation = passwordConfirmation), csrfToken)
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val authResponse = response.body<AuthResponseDto>()
                if (authResponse.accessToken != null) {
                    dataStore.saveToken(authResponse.accessToken)
                }
                dataStore.saveUser(name, email)
                AuthResult.Success(Unit)
            } else {
                handleResponseError(response)
            }
        } catch (e: Exception) {
            AuthResult.Error("Erreur lors de l'inscription.")
        }
    }

    override suspend fun getUser(): AuthResult<User> {
        val cached = cachedUser()
        return try {
            val token = dataStore.authToken.firstOrNull() ?: ""
            val response = api.getUser(token)
            if (response.status == HttpStatusCode.OK) {
                val bodyText = response.bodyAsText()
                val json = Json.parseToJsonElement(bodyText).jsonObject
                val userObj = json["user"]?.jsonObject ?: json
                val userDto = Json { ignoreUnknownKeys = true }.decodeFromJsonElement(UserDto.serializer(), userObj)
                dataStore.saveUser(userDto.name, userDto.email)
                AuthResult.Success(userDto.toDomain())
            } else {
                cached?.let { AuthResult.Success(it) } ?: AuthResult.Error("Impossible de charger le profil.")
            }
        } catch (e: Exception) {
            cached?.let { AuthResult.Success(it) } ?: AuthResult.Error("Session expirée ou erreur réseau.")
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        val token = dataStore.authToken.firstOrNull() ?: ""
        // Nettoyage immédiat et garanti des identifiants locaux
        dataStore.clearToken()
        dataStore.clearUser()

        // Notification non-bloquante au serveur
        if (token.isNotBlank()) {
            try {
                withTimeoutOrNull(2000L) {
                    val csrfToken = api.getCsrfToken()
                    api.logout(token, csrfToken)
                }
            } catch (e: Exception) {
                // Erreur réseau ignorée pour ne pas bloquer la déconnexion locale
            }
        }
        return AuthResult.Success(Unit)
    }

    override suspend fun updateProfile(name: String, email: String): AuthResult<Unit> {
        return try {
            val token = dataStore.authToken.firstOrNull() ?: ""
            val csrfToken = api.getCsrfToken()
            val response = api.updateProfile(UpdateProfileRequestDto(name, email), token, csrfToken)
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted) {
                dataStore.saveUser(name, email)
                AuthResult.Success(Unit)
            } else {
                handleResponseError(response)
            }
        } catch (e: Exception) {
            AuthResult.Error("Mise à jour impossible : ${e.localizedMessage ?: "Erreur réseau"}")
        }
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String, newPasswordConfirmation: String): AuthResult<Unit> {
        return try {
            val token = dataStore.authToken.firstOrNull() ?: ""
            val csrfToken = api.getCsrfToken()
            val response = api.updatePassword(UpdatePasswordRequestDto(currentPassword, newPassword, newPasswordConfirmation), token, csrfToken)
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Accepted || response.status == HttpStatusCode.NoContent) {
                AuthResult.Success(Unit)
            } else {
                handleResponseError(response)
            }
        } catch (e: Exception) {
            AuthResult.Error("Échec du changement de mot de passe : ${e.localizedMessage ?: "Erreur réseau"}")
        }
    }

    override suspend fun isLoggedIn(): Boolean = dataStore.authToken.firstOrNull() != null

    private suspend fun cachedUser(): User? {
        val name = dataStore.userName.firstOrNull()
        val email = dataStore.userEmail.firstOrNull()
        return if (!name.isNullOrBlank() && !email.isNullOrBlank()) {
            User(name = name, email = email)
        } else {
            null
        }
    }

    private fun UserDto.toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        role = role
    )

    private suspend fun handleResponseError(response: HttpResponse): AuthResult.Error {
        val body = try { response.bodyAsText() } catch (e: Exception) { "" }
        return try {
            val json = Json.parseToJsonElement(body).jsonObject
            val errorsObject = json["errors"]?.jsonObject ?: json
            val fieldErrors = mutableMapOf<String, List<String>>()
            
            errorsObject.forEach { (key, value) ->
                if (key != "message") {
                    when (value) {
                        is JsonArray -> fieldErrors[key] = value.map { it.jsonPrimitive.content }
                        else -> fieldErrors[key] = listOf(value.jsonPrimitive.content)
                    }
                }
            }
            
            val message = json["message"]?.jsonPrimitive?.content ?: "Données invalides"
            AuthResult.Error(message, fieldErrors)
        } catch (e: Exception) {
            AuthResult.Error("Erreur serveur (${response.status.value})")
        }
    }
}
