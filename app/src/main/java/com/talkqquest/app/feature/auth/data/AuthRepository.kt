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

    suspend fun loginWithEmail(email: String, password: String): ApiResult<EmailLoginData> {
        val result = try {
            safeApiCall {
                authApi.loginWithEmail(
                    EmailLoginRequest(
                        email = email,
                        password = password,
                    ),
                )
            }
        } catch (e: HttpException) {
            ApiResult.Error(code = e.code(), message = emailLoginErrorMessage(e.code()))
        }
        if (result is ApiResult.Success) {
            tokenDataStore.saveTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
            )
        }
        return if (result is ApiResult.Error && result.code != null) {
            result.copy(message = emailLoginErrorMessage(result.code))
        } else {
            result
        }
    }

    suspend fun requestEmailCode(email: String): ApiResult<Unit> =
        callUnitApi { authApi.requestEmailCode(EmailCodeRequest(email = email)) }

    suspend fun verifyEmailCode(email: String, code: String): ApiResult<Unit> =
        callUnitApi { authApi.verifyEmailCode(EmailVerifyRequest(email = email, code = code)) }

    suspend fun refreshAccessToken(refreshToken: String): ApiResult<TokenRefreshData> {
        val result = safeApiCall {
            authApi.refreshAccessToken(TokenRefreshRequest(refreshToken = refreshToken))
        }
        if (result is ApiResult.Success) {
            tokenDataStore.saveAccessToken(result.data.accessToken)
        }
        return result
    }


    suspend fun saveOnboardingStep(request: OnboardingStepSaveRequest): ApiResult<OnboardingStepSaveData> {
        val result = safeApiCall { authApi.saveOnboardingStep(request) }
        return if (result is ApiResult.Error && result.code != null) {
            result.copy(message = onboardingErrorMessage(result.code))
        } else {
            result
        }
    }

    suspend fun completeOnboarding(): ApiResult<OnboardingCompleteData> {
        val result = safeApiCall { authApi.completeOnboarding() }
        return if (result is ApiResult.Error && result.code != null) {
            result.copy(message = onboardingCompleteErrorMessage(result.code))
        } else {
            result
        }
    }
    suspend fun signupWithEmail(request: EmailSignupRequest): ApiResult<EmailSignupData> {
        val result = safeApiCall { authApi.signupWithEmail(request) }
        if (result is ApiResult.Success) {
            tokenDataStore.saveTokens(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
            )
        }
        return if (result is ApiResult.Error && result.code != null) {
            result.copy(message = emailSignupErrorMessage(result.code))
        } else {
            result
        }
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

    private fun emailLoginErrorMessage(code: Int): String = when (code) {
        403 -> "\uD0C8\uD1F4\uD55C \uACC4\uC815\uC785\uB2C8\uB2E4."
        500 -> "\uC11C\uBC84 \uB0B4\uBD80 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
        else -> "\uC774\uBA54\uC77C \uB610\uB294 \uBE44\uBC00\uBC88\uD638\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694."
    }

    private fun emailSignupErrorMessage(code: Int): String = when (code) {
        400 -> "\uC785\uB825\uAC12\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694."
        409 -> "\uC774\uBBF8 \uC0AC\uC6A9 \uC911\uC778 \uC774\uBA54\uC77C\uC785\uB2C8\uB2E4."
        422 -> "\uC774\uBA54\uC77C \uC778\uC99D\uC774 \uC644\uB8CC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4."
        500 -> "\uC11C\uBC84 \uB0B4\uBD80 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
        else -> "\uD68C\uC6D0\uAC00\uC785\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694."
    }

    private fun emailAuthErrorMessage(code: Int): String = when (code) {
        400 -> "\uC774\uBA54\uC77C \uB610\uB294 \uC778\uC99D\uBC88\uD638 \uD615\uC2DD\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694."
        409 -> "\uC774\uBBF8 \uAC00\uC785\uB41C \uC774\uBA54\uC77C\uC785\uB2C8\uB2E4."
        410 -> "\uC778\uC99D \uCF54\uB4DC\uAC00 \uB9CC\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uB2E4\uC2DC \uC694\uCCAD\uD574\uC8FC\uC138\uC694."
        500 -> "\uC11C\uBC84 \uB0B4\uBD80 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
        else -> "\uC694\uCCAD\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694."
    }


    private fun onboardingErrorMessage(code: Int): String = when (code) {
        400 -> "\uC120\uD0DD\uAC12\uC744 \uD655\uC778\uD574\uC8FC\uC138\uC694."
        401 -> "\uB85C\uADF8\uC778\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."
        500 -> "\uC11C\uBC84 \uB0B4\uBD80 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
        else -> "\uC628\uBCF4\uB529 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694."
    }

    private fun onboardingCompleteErrorMessage(code: Int): String = when (code) {
        400 -> "\uC628\uBCF4\uB529\uC774 \uC544\uC9C1 \uC644\uB8CC\uB418\uC9C0 \uC54A\uC558\uC5B4\uC694."
        401 -> "\uB85C\uADF8\uC778\uC774 \uD544\uC694\uD569\uB2C8\uB2E4."
        409 -> "\uC774\uBBF8 \uC628\uBCF4\uB529\uC774 \uC644\uB8CC\uB41C \uACC4\uC815\uC785\uB2C8\uB2E4."
        500 -> "\uC11C\uBC84 \uB0B4\uBD80 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
        else -> "\uC628\uBCF4\uB529 \uC644\uB8CC \uCC98\uB9AC\uC5D0 \uC2E4\uD328\uD588\uC5B4\uC694."
    }
    private fun currentDeviceInfo(): DeviceInfo = DeviceInfo(
        platform = "android",
        model = Build.MODEL.orEmpty(),
        osVersion = Build.VERSION.RELEASE.orEmpty(),
    )
}

