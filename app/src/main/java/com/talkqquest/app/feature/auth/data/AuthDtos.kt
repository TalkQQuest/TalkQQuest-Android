package com.talkqquest.app.feature.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class SocialLoginRequest(
    val providerAccessToken: String,
    val deviceInfo: DeviceInfo? = null,
)

@Serializable
data class DeviceInfo(
    val platform: String,
    val model: String,
    val osVersion: String,
    val fcmToken: String? = null,
)

@Serializable
data class SocialLoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null,
    val isNewUser: Boolean = false,
    val needsLinking: Boolean = false,
    val user: SocialLoginUser,
)

@Serializable
data class SocialLoginUser(
    val id: String,
    val email: String? = null,
    val nickname: String? = null,
    val provider: String,
)