package com.talkqquest.app.feature.auth.data

import com.talkqquest.app.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun loginWithEmail(
        @Body request: EmailLoginRequest,
    ): ApiResponse<EmailLoginData>

    @POST("api/v1/auth/oauth/kakao")
    suspend fun loginWithKakao(
        @Body request: SocialLoginRequest,
    ): ApiResponse<SocialLoginData>

    @POST("api/v1/auth/oauth/naver")
    suspend fun loginWithNaver(
        @Body request: SocialLoginRequest,
    ): ApiResponse<SocialLoginData>

    @POST("api/v1/auth/email/request")
    suspend fun requestEmailCode(
        @Body request: EmailCodeRequest,
    ): ApiResponse<Unit>

    @POST("api/v1/auth/email/verify")
    suspend fun verifyEmailCode(
        @Body request: EmailVerifyRequest,
    ): ApiResponse<Unit>

    @POST("api/v1/auth/signup")
    suspend fun signupWithEmail(
        @Body request: EmailSignupRequest,
    ): ApiResponse<EmailSignupData>
}