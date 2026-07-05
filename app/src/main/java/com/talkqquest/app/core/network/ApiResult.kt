package com.talkqquest.app.core.network

// API 호출 결과 래퍼 (성공/실패/예외).
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val code: Int?, val message: String?) : ApiResult<Nothing>
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>
}
