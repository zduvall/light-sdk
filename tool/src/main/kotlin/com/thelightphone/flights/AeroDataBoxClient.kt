package com.thelightphone.flights

import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>

    sealed interface Error : ApiResult<Nothing> {
        data class Http(val code: Int, val message: String? = null) : Error
        data object Unauthorized : Error    // 401, 403
        data object NotFound : Error        // 404
        data object RateLimited : Error     // 429
        data object MissingAuth : Error     // Empty key locally
        data class Network(val throwable: Throwable) : Error
        data class Unknown(val throwable: Throwable? = null) : Error
    }
}

object FlightAppNetwork {
    val httpClient = HttpClient(OkHttp) {
        // Allow API to return fields not defined on models without crashing
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
}

class AeroDataBoxClient(
    private val client: HttpClient = FlightAppNetwork.httpClient,
    private val settingsRepository: SettingsRepository
) {
    private val host = "aerodatabox.p.rapidapi.com"
    private val baseUrl = "https://$host"

    suspend fun fetchUsageLimits(): ApiResult<UsageLimits> =
        get("/subscriptions/balance") { response ->
            UsageLimits(
                requestsRemaining = response.headers["x-ratelimit-requests-remaining"]?.toIntOrNull() ?: 0,
                requestsReset = response.headers["x-ratelimit-requests-reset"]?.toLongOrNull() ?: 0L,
                unitsRemaining = response.headers["x-ratelimit-api-units-remaining"]?.toIntOrNull() ?: 0,
                unitsReset = response.headers["x-ratelimit-api-units-reset"]?.toLongOrNull() ?: 0L
            )
        }

    suspend fun fetchFlightStatus(
        flightNumber: String
    ): ApiResult<List<FlightStatus>> =
        get(
            path = "/flights/number/$flightNumber",
            statusErrors = listOf(204 to ApiResult.Error.NotFound),
            configure = {
                url {
                    parameters.append("withFlightPlan", "false")
                    parameters.append("withLocation", "false")
                    parameters.append("withAircraftImage", "false")
                }
            }
        ) { response ->
            response.body<List<FlightStatus>>()
        }

    private suspend fun <T> get(
        path: String,
        statusErrors: List<Pair<Int, ApiResult.Error>> = emptyList(),
        configure: HttpRequestBuilder.() -> Unit = {},
        parse: suspend (HttpResponse) -> T
    ): ApiResult<T> {
        val apiKey = settingsRepository.apiKeyFlow.first()

        if (apiKey.isBlank()) return ApiResult.Error.MissingAuth

        return try {
            val response = client.get("$baseUrl$path") {
                configure()
                header("x-rapidapi-key", apiKey)
                header("x-rapidapi-host", host)
            }

            val status = response.status.value
            val customError = statusErrors.firstOrNull { it.first == status }?.second

            when {
                customError != null -> customError
                status == 200 -> ApiResult.Success(parse(response))
                status == 401 || status == 403 -> ApiResult.Error.Unauthorized
                status == 429 -> ApiResult.Error.RateLimited
                else -> ApiResult.Error.Http(status, response.status.description)
            }
        } catch (e: Exception) {
            ApiResult.Error.Network(e)
        }
    }
}