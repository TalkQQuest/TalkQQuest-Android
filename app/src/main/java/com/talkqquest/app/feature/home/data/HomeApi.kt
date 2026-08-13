package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.home.data.model.ArchiveSummary
import com.talkqquest.app.feature.home.data.model.CurrentPasswordVerifyRequest
import com.talkqquest.app.feature.home.data.model.HomeSummary
import com.talkqquest.app.feature.home.data.model.LegalDocument
import com.talkqquest.app.feature.home.data.model.MyBadgesResponse
import com.talkqquest.app.feature.home.data.model.MyPageDashboard
import com.talkqquest.app.feature.home.data.model.PasswordChangeRequest
import com.talkqquest.app.feature.home.data.model.ProfileImageUploadResponse
import com.talkqquest.app.feature.home.data.model.UserMe
import com.talkqquest.app.feature.home.data.model.UserSettings
import com.talkqquest.app.feature.home.data.model.UserSettingsUpdateRequest
import com.talkqquest.app.feature.home.data.model.UserUpdateRequest
import com.talkqquest.app.feature.mission.data.model.XpSummary
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST

interface HomeApi {

    @GET("api/v1/home/summary")
    suspend fun getHomeSummary(): ApiResponse<HomeSummary>

    @GET("api/v1/users/me")
    suspend fun getMe(): ApiResponse<UserMe>

    @GET("api/v1/users/me/dashboard")
    suspend fun getMyPageDashboard(): ApiResponse<MyPageDashboard>

    @GET("api/v1/xp/summary")
    suspend fun getXpSummary(): ApiResponse<XpSummary>

    @PATCH("api/v1/users/me")
    suspend fun updateMe(@Body request: UserUpdateRequest): ApiResponse<Unit>

    @POST("api/v1/users/me/password/verify")
    suspend fun verifyPassword(@Body request: CurrentPasswordVerifyRequest): ApiResponse<Unit>

    @PATCH("api/v1/users/me/password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): ApiResponse<Unit>

    @GET("api/v1/badges/me")
    suspend fun getMyBadges(): ApiResponse<MyBadgesResponse>

    @GET("api/v1/archives/summary")
    suspend fun getArchiveSummary(): ApiResponse<ArchiveSummary>

    @GET("api/v1/legal/terms")
    suspend fun getServiceTerms(): ApiResponse<LegalDocument>

    @GET("api/v1/legal/privacy")
    suspend fun getPrivacyPolicy(): ApiResponse<LegalDocument>

    @GET("api/v1/users/me/settings")
    suspend fun getUserSettings(): ApiResponse<UserSettings>

    @PATCH("api/v1/users/me/settings")
    suspend fun updateUserSettings(@Body request: UserSettingsUpdateRequest): ApiResponse<Unit>

    @Multipart
    @POST("api/v1/uploads/profile-image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): ApiResponse<ProfileImageUploadResponse>

}
