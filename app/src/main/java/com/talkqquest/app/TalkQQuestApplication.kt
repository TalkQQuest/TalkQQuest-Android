package com.talkqquest.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NidOAuth
import com.talkqquest.app.core.push.PushNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TalkQQuestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // FCM 알림 채널 생성 (없으면 알림이 안 뜸)
        PushNotifications.createDefaultChannel(this)

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