package com.talkqquest.app.feature.profile.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.feature.home.data.HomeApi
import com.talkqquest.app.feature.home.data.model.CurrentPasswordVerifyRequest
import com.talkqquest.app.feature.home.data.model.LegalDocument
import com.talkqquest.app.feature.home.data.model.MyBadgesResponse
import com.talkqquest.app.feature.home.data.model.MyPageDashboard
import com.talkqquest.app.feature.home.data.model.PasswordChangeRequest
import com.talkqquest.app.feature.home.data.model.ProfileImageUploadResponse
import com.talkqquest.app.feature.home.data.model.UserMe
import com.talkqquest.app.feature.home.data.model.UserSettings
import com.talkqquest.app.feature.home.data.model.UserSettingsUpdateRequest
import com.talkqquest.app.feature.home.data.model.UserUpdateRequest
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val homeApi: HomeApi,
) {
    suspend fun getMyProfile(): ApiResult<UserMe> =
        serverCall { homeApi.getMe() }

    suspend fun getMyPageDashboard(): ApiResult<MyPageDashboard> =
        serverCall { homeApi.getMyPageDashboard() }

    suspend fun getMyBadges(): ApiResult<MyBadgesResponse> =
        serverCall { homeApi.getMyBadges() }

    suspend fun getServiceTerms(): ApiResult<LegalDocument> =
        serverCall { homeApi.getServiceTerms() }

    suspend fun getPrivacyPolicy(): ApiResult<LegalDocument> =
        serverCall { homeApi.getPrivacyPolicy() }

    suspend fun getUserSettings(): ApiResult<UserSettings> =
        serverCall { homeApi.getUserSettings() }

    suspend fun uploadProfileImage(image: MultipartBody.Part): ApiResult<ProfileImageUploadResponse> =
        serverCall { homeApi.uploadProfileImage(image) }
    suspend fun updateUserSettings(request: UserSettingsUpdateRequest): ApiResult<Unit> =
        try {
            val response = homeApi.updateUserSettings(request)
            if (response.success) {
                ApiResult.Success(Unit)
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

    suspend fun updateProfile(request: UserUpdateRequest): ApiResult<Unit> =
        try {
            val response = homeApi.updateMe(request)
            if (response.success) {
                ApiResult.Success(Unit)
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

    suspend fun verifyCurrentPassword(currentPassword: String): ApiResult<Unit> =
        try {
            val response = homeApi.verifyPassword(CurrentPasswordVerifyRequest(currentPassword = currentPassword))
            if (response.success) {
                ApiResult.Success(Unit)
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

    suspend fun changePassword(newPassword: String): ApiResult<Unit> =
        try {
            val response = homeApi.changePassword(PasswordChangeRequest(newPassword = newPassword))
            if (response.success) {
                ApiResult.Success(Unit)
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
}