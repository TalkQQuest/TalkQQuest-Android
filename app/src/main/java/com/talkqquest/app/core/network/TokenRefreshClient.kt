package com.talkqquest.app.core.network

import com.talkqquest.app.core.datastore.TokenDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// AuthInterceptor에서만 사용하는 토큰 재발급 클라이언트입니다.
// 인터셉터가 붙지 않은 OkHttpClient를 사용해야 refresh 요청이 다시 인터셉터를 타는 순환을 피할 수 있습니다.
@Singleton
class TokenRefreshClient @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) {
    private val refreshUrl = "https://talkqquest.shop/api/v1/auth/refresh"
    private val client = OkHttpClient()

    @Synchronized
    fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val body = JSONObject()
                .put("refreshToken", refreshToken)
                .toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(refreshUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val json = JSONObject(response.body?.string() ?: return null)
                if (!json.optBoolean("success")) return null
                val accessToken = json.optJSONObject("data")
                    ?.optString("accessToken")
                    ?.takeIf { it.isNotBlank() }
                    ?: return null

                runBlocking { tokenDataStore.saveAccessToken(accessToken) }
                accessToken
            }
        } catch (e: Exception) {
            null
        }
    }
}