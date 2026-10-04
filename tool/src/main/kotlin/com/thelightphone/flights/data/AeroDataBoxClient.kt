package com.thelightphone.flights.data

import com.thelightphone.flights.model.ApiResult
import com.thelightphone.flights.model.FlightAppNetwork
import com.thelightphone.flights.model.Flight
import com.thelightphone.flights.model.UsageLimits
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first

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
    ): ApiResult<List<Flight>> =
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
            response.body<List<Flight>>()
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
