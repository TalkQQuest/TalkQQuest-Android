package com.talkqquest.app.feature.archive.di

import com.talkqquest.app.feature.archive.data.ArchiveApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// 아카이브 기능 전용 Hilt 모듈.
@Module
@InstallIn(SingletonComponent::class)
object ArchiveModule {

    @Provides
    @Singleton
    fun provideArchiveApi(retrofit: Retrofit): ArchiveApi =
        retrofit.create(ArchiveApi::class.java)