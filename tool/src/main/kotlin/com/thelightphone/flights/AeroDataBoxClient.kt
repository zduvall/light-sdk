package com.thelightphone.flights

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.first

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>

    sealed interface Error : ApiResult<Nothing> {
        data class Http(val code: Int, val message: String? = null) : Error
        data object Unauthorized : Error    // 401, 403
        data object RateLimited : Error     // 429
        data object MissingAuth : Error     // Empty key locally
        data class Network(val throwable: Throwable) : Error
        data class Unknown(val throwable: Throwable? = null) : Error
    }
}

object FlightAppNetwork {
    val httpClient = HttpClient(OkHttp)
}

class AeroDataBoxClient(
    private val client: HttpClient = FlightAppNetwork.httpClient,
    private val settingsRepository: SettingsRepository
) {
    private val host = "aerodatabox.p.rapidapi.com"
    private val baseUrl = "https://$host"

    suspend fun fetchUsageLimits(): ApiResult<UsageLimits> {
        val apiKey = settingsRepository.apiKeyFlow.first()
        
        if (apiKey.isBlank()) return ApiResult.Error.MissingAuth

        return try {
            val response: HttpResponse = client.get("$baseUrl/subscriptions/balance") {
                header("x-rapidapi-key", apiKey)
                header("x-rapidapi-host", host)
            }
            
            when (response.status.value) {
                in 200..299 -> {
                    val limits = UsageLimits(
                        requestsRemaining = response.headers["x-ratelimit-requests-remaining"]?.toIntOrNull() ?: 0,
                        requestsReset = response.headers["x-ratelimit-requests-reset"]?.toLongOrNull() ?: 0L,
                        unitsRemaining = response.headers["x-ratelimit-api-units-remaining"]?.toIntOrNull() ?: 0,
                        unitsReset = response.headers["x-ratelimit-api-units-reset"]?.toLongOrNull() ?: 0L
                    )
                    ApiResult.Success(limits)
                }
                401, 403 -> ApiResult.Error.Unauthorized
                429 -> ApiResult.Error.RateLimited
                else -> ApiResult.Error.Http(response.status.value, response.status.description)
            }
        } catch (e: Exception) {
            ApiResult.Error.Network(e)
        }
    }
}