package com.talkqquest.app.feature.home.di

import com.talkqquest.app.feature.home.data.HomeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// 홈 기능 전용 Hilt 모듈 (예시).
// core/di/NetworkModule가 만들어 준 Retrofit을 받아 HomeApi 구현체를 생성해 제공합니다.
// 각 기능은 자기 Api마다 이렇게 provideXxxApi 를 하나씩 만들면 됩니다.
@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi =
        retrofit.create(HomeApi::class.java)
}
