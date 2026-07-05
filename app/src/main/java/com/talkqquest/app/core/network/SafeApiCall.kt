package com.talkqquest.app.core.network

import retrofit2.HttpException
import java.io.IOException

// API 호출을 감싸 성공/실패/예외를 ApiResult로 변환.
// Repository에서: safeApiCall { api.getHomeSummary() } → ApiResult<HomeSummary>
// 네트워크 예외(끊김/서버에러)를 잡아 크래시를 막고, 서버 공통응답(success/data)을 풀어줍니다.
//
// 주의: 데이터 없이 성공만 주는 API(로그아웃/삭제 등, data == null)는 아래 조건상 Error로 처리됩니다.
// 그런 API는 safeApiCall을 쓰지 말고 Repository에서 성공 여부(success)만 직접 확인하세요.
suspend fun <T> safeApiCall(call: suspend () -> ApiResponse<T>): ApiResult<T> =
    try {
        val response = call()
        val data = response.data
        if (response.success && data != null) {
            ApiResult.Success(data)
        } else {
            ApiResult.Error(code = null, message = response.message)
        }
    } catch (e: HttpException) {
        ApiResult.Error(code = e.code(), message = e.message())
    } catch (e: IOException) {
        ApiResult.Exception(e)
    } catch (e: Exception) {
        ApiResult.Exception(e)
    }
