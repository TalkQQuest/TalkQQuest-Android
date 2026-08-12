package com.talkqquest.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NidOAuth
import com.talkqquest.app.core.push.PushNotifications
import com.talkqquest.app.core.push.PushTokenRegistrar
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TalkQQuestApplication : Application() {
    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar

    override fun onCreate() {
        super.onCreate()

        // FCM 기본 알림 채널을 생성하고, 저장된 로그인 세션이 있으면 현재 기기 토큰을 서버에 등록한다.
        PushNotifications.createDefaultChannel(this)
        pushTokenRegistrar.registerCurrentToken()

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
