package com.talkqquest.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NidOAuth
import com.talkqquest.app.core.push.FcmTokenRegistrar
import com.talkqquest.app.core.push.PushNotifications
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TalkQQuestApplication : Application() {
    @Inject lateinit var fcmTokenRegistrar: FcmTokenRegistrar
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // FCM 알림 채널 생성 (없으면 알림이 안 뜸)
        PushNotifications.createDefaultChannel(this)
        applicationScope.launch { fcmTokenRegistrar.registerCurrentTokenIfAuthenticated() }

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        if (BuildConfig.NAVER_CLIENT_ID.isNotBlank() && BuildConfig.NAVER_CLIENT_SECRET.isNotBlank()) {
            NidOAuth.initialize(
                this,
                BuildConfig.NAVER_CLIENT_ID,
                BuildConfig.NAVER_CLIENT_SECRET,
                BuildConfig.NAVER_CLIENT_NAME,
            )
        }
    }
}