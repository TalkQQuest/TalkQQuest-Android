package com.talkqquest.app.feature.auth.data

import android.os.Build
import com.talkqquest.app.core.datastore.TokenDataStore
import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun loginWithKakao(providerAccessToken: String): ApiResult<SocialLoginData> =
        loginWithProvider(providerAccessToken) { request -> authApi.loginWithKakao(request) }

    suspend fun loginWithNaver(providerAccessToken: String): ApiResult<SocialLoginData> =
        loginWithProvider(providerAccessToken) { request -> authApi.loginWithNaver(request) }

    suspend fun requestEmailCode(email: String): ApiResult<Unit> =
        callUnitApi { authApi.requestEmailCode(EmailCodeRequest(email = email)) }

    suspend fun verifyEmailCode(email: String, code: String): ApiResult<Unit> =
        callUnitApi { authApi.verifyEmailCode(EmailVerifyRequest(email = email, code = code)) }

    suspend fun signupWithEmail(request: EmailSignupRequest): ApiResult<EmailSignupData> {
        val result = safeApiCall { authApi.signupWithEmail(request) }
        if (result is ApiResult.Success) {
            tokenDataStore.saveTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
            )
        }
        return result
    }

    private suspend fun loginWithProvider(
        providerAccessToken: String,
        call: suspend (SocialLoginRequest) -> ApiResponse<SocialLoginData>,
    ): ApiResult<SocialLoginData> {
        val result = safeApiCall {
            call(
                SocialLoginRequest(
                    providerAccessToken = providerAccessToken,
                    deviceInfo = currentDeviceInfo(),
                ),
            )
        }
        if (result is ApiResult.Success) {
            tokenDataStore.saveTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
            )
        }
        return result
    }

    private suspend fun callUnitApi(call: suspend () -> ApiResponse<Unit>): ApiResult<Unit> =
        try {
            val response = call()
            if (response.success) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(code = null, message = response.message)
            }
        } catch (e: HttpException) {
            ApiResult.Error(code = e.code(), message = emailAuthErrorMessage(e.code()))
        } catch (e: IOException) {
            ApiResult.Exception(e)
        } catch (e: Exception) {
            ApiResult.Exception(e)
        }

    private fun emailAuthErrorMessage(code: Int): String = when (code) {
        400 -> "이메일 또는 인증번호 형식을 확인해주세요."
        409 -> "이미 가입된 이메일입니다."
        410 -> "인증 코드가 만료되었습니다. 다시 요청해주세요."
        500 -> "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        else -> "요청에 실패했어요."
    }
    private fun currentDeviceInfo(): DeviceInfo = DeviceInfo(
        platform = "android",
        model = Build.MODEL.orEmpty(),
        osVersion = Build.VERSION.RELEASE.orEmpty(),
    )
}