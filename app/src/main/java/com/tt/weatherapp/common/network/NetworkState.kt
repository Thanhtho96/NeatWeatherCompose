package com.tt.weatherapp.common.network

enum class NetworkState(val message: String) {
    NO_INTERNET("NO INTERNET"),
    BAD_REQUEST("BAD REQUEST"), // 400 Bad Request
    NOT_FOUND("NOT FOUND"), // 404 NotFound
    UNAUTHORISED("UNAUTHORISED"), // 401 UnauthorizedError
    FORBIDDEN("FORBIDDEN"), // 403 Forbidden
}

data class NetworkError(val networkState: NetworkState, val message: String?)