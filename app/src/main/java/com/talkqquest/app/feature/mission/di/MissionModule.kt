package com.talkqquest.app.feature.mission.di

import com.talkqquest.app.feature.mission.data.MissionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// 미션 기능 전용 Hilt 모듈. core/di/NetworkModule의 Retrofit으로 MissionApi 구현체 제공.
@Module
@InstallIn(SingletonComponent::class)
object MissionModule {

    @Provides
    @Singleton
    fun provideMissionApi(retrofit: Retrofit): MissionApi =
        retrofit.create(MissionApi::class.java)
}
