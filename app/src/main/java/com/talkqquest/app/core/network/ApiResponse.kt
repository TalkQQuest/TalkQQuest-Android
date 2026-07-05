package com.talkqquest.app.core.network

import kotlinx.serialization.Serializable

// 서버 공통 응답 형식 (기능명세서: success, message, data). 모든 API가 이 형태로 감쌉니다.
// 공통 에러 코드: VALIDATION_ERROR, UNAUTHORIZED, NOT_FOUND, DUPLICATED, SERVER_ERROR
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
)
