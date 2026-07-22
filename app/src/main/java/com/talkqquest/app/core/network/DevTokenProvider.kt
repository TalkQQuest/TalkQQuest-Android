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

// ── 개발용 자동 로그인 (임시) ──
// 로그인 화면(A담당)이 앱 시작점이 되기 전까지, 토큰이 비어 있으면 팀 테스트 계정으로
// 로그인해 TokenDataStore를 채운다. AuthInterceptor가 사용 — 토큰이 1시간 만료돼도
// AuthInterceptor의 401 분기가 이걸 다시 불러 자동 복구되므로, 개발 중엔 만료를 느낄 일이 없음.
// (단, 화면에서 실제 로그인한 세션은 여기로 복구하지 않음 — 이유는 AuthInterceptor 주석 참고)
// TODO(A/로그인): 로그인 화면이 시작점으로 연결되면 이 파일 삭제 + AuthInterceptor에서 참조 제거.
@Singleton
class DevTokenProvider @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) {
    // 팀 공용 테스트 계정 (백엔드 제공, 디스코드 공유분)
    private val loginUrl = "https://talkqquest.shop/api/v1/auth/login"
    private val testEmail = "heesu15047@gmail.com"
    private val testPassword = "Pass1234!"

    // 인터셉터와 별도의 순수 클라이언트 — AuthInterceptor를 끼우면 순환 호출이 됨.
    private val client = OkHttpClient()

    // 여러 요청이 동시에 401을 맞아도 로그인은 한 번만 하도록 잠금.
    @Synchronized
    fun loginAndCache(): String? {
        return try {
            val body = JSONObject()
                .put("email", testEmail)
                .put("password", testPassword)
                .toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(loginUrl).post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val json = JSONObject(response.body?.string() ?: return null)
                if (!json.optBoolean("success")) return null
                val token = json.optJSONObject("data")?.optString("accessToken")
                    ?.takeIf { it.isNotBlank() } ?: return null
                runBlocking { tokenDataStore.saveAccessToken(token) }
                token
            }
        } catch (e: Exception) {
            null // 오프라인 등 — 토큰 없이 진행 (화면은 stub 폴백)
        }
    }
}
