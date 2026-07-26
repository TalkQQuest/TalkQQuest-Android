package com.talkqquest.app.core.network

import com.talkqquest.app.core.DemoConfig
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

// 데모 목업 스위치용 래퍼. DemoConfig.USE_MOCK이면 서버 호출을 건너뛰고 실패로 처리해
// Repository의 stub 폴백 경로를 타게 한다(= 서버 대신 목업 표시). false면 safeApiCall과 동일.
// 서버 연동 코드는 그대로 두고 스위치 하나로 목업↔서버를 전환하기 위함.
suspend fun <T> serverCall(call: suspend () -> ApiResponse<T>): ApiResult<T> =
    if (DemoConfig.USE_MOCK) ApiResult.Error(code = null, message = "mock mode") else safeApiCall(call)
