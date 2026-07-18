package com.talkqquest.app.feature.auth.data

import android.os.Build
import com.talkqquest.app.core.datastore.TokenDataStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun loginWithKakao(providerAccessToken: String): ApiResult<SocialLoginData> =
        loginWithProvider(providerAccessToken) { request -> authApi.loginWithKakao(request) }

    suspend fun loginWithNaver(providerAccessToken: String): ApiResult<SocialLoginData> =
        loginWithProvider(providerAccessToken) { request -> authApi.loginWithNaver(request) }

    private suspend fun loginWithProvider(
        providerAccessToken: String,
        call: suspend (SocialLoginRequest) -> com.talkqquest.app.core.network.ApiResponse<SocialLoginData>,
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

    private fun currentDeviceInfo(): DeviceInfo = DeviceInfo(
        platform = "android",
        model = Build.MODEL.orEmpty(),
        osVersion = Build.VERSION.RELEASE.orEmpty(),
    )
}