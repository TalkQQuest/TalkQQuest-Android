package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResponse
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import retrofit2.http.GET

// 아카이브 API (기능명세서 F101~F103). Retrofit 인터페이스.
interface ArchiveApi {

    // 아카이브 카운트 및 최근 활동 요약 조회 (F101)
    @GET("api/v1/archives/summary")
    suspend fun getArchiveSummary(): ApiResponse<ArchiveSummary>

    // 추가 API (참고용 - 명세서 F102, F103 등)
    // @GET("api/v1/archives/search")
    // suspend fun searchArchives(...)
}