package com.talkqquest.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "talkqquest_prefs")

// 로그인 토큰 저장소. 저장한 토큰은 AuthInterceptor가 요청에 붙임.
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[accessTokenKey] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs -> prefs[refreshTokenKey] }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { prefs -> prefs[accessTokenKey] = token }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}