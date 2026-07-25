package com.talkqquest.app.feature.report.di

import com.talkqquest.app.feature.report.data.ReportApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// 리포트 기능 전용 Hilt 모듈. core/di/NetworkModule의 Retrofit으로 ReportApi 구현체 제공.
@Module
@InstallIn(SingletonComponent::class)
object ReportModule {

    @Provides
    @Singleton
    fun provideReportApi(retrofit: Retrofit): ReportApi =
        retrofit.create(ReportApi::class.java)
}
