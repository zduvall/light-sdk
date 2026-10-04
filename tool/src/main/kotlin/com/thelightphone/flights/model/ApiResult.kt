package com.thelightphone.flights.model

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
