package com.network.data

sealed class ApiResult<T> {
    class Success<T>(val data:T) : ApiResult<T>()
    class Error<T>(val th: Throwable) : ApiResult<T>()
}