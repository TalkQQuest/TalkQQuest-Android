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

        // FCM 알림 채널 생성 (없으면 알림이 안 뜸)
        PushNotifications.createDefaultChannel(this)

        // 이 기기의 FCM 토큰을 서버에 등록. 토큰 발급 시점(onNewToken)은 보통 설치 직후 한 번뿐이라
        // 이미 깔려 있던 기기는 그때가 지나 있다. 앱 시작마다 현재 토큰을 다시 올려 채운다.
        // 로그인 전이면 401로 끝나고, 다음 실행(로그인 후)에서 다시 올라간다.
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