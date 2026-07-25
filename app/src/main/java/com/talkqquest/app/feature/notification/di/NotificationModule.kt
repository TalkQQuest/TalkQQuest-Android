package com.talkqquest.app.feature.notification.di

import com.talkqquest.app.feature.notification.data.NotificationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// 알림 기능 전용 Hilt 모듈. core/di/NetworkModule의 Retrofit으로 NotificationApi 구현체 제공.
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)
}
