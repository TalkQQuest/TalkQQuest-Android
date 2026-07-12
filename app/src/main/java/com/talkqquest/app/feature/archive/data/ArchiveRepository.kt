package com.talkqquest.app.feature.archive.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.archive.data.model.ArchiveRecentActivity
import com.talkqquest.app.feature.archive.data.model.ArchiveSummary
import javax.inject.Inject

// 아카이브 Repository. ViewModel과 API 사이를 잇는 계층.
class ArchiveRepository @Inject constructor(
    private val archiveApi: ArchiveApi
) {
    // TODO(서버 연동 전 임시): 백엔드 아카이브 API가 붙으면 아래 stub 리턴을 지우고 주석 처리된 리턴으로 복구.
    // suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> = safeApiCall { archiveApi.getArchiveSummary() }

    suspend fun getArchiveSummary(): ApiResult<ArchiveSummary> =
        ApiResult.Success(stubArchiveSummary)
}

// 서버 없이 에뮬레이터에서 아카이브 화면 확인용 임시 데이터. 서버 연동 시 이 값째로 삭제.
private val stubArchiveSummary = ArchiveSummary(
    completedMissionCount = 3,
    conversationCount = 3,
    savedSentenceCount = 2,
    reportCount = 3,
    recentActivities = listOf(
        ArchiveRecentActivity("1", "MISSION", "처음 보는 사람에게 짧게 인사하기", "미션 완료", "2026.08.20"),
        ArchiveRecentActivity("2", "CONVERSATION", "처음 보는 사람에게 짧게 인사하기", "대화 완료", "2026.08.20"),
        ArchiveRecentActivity("3", "SENTENCE", "\"그렇군요! 저도 편해서 놀랐습니다. 혹시라도 불편하신 점 있다면 알려주세요!!\"", "문장 저장", "2026.08.20"),
        ArchiveRecentActivity("4", "REPORT", "처음 보는 사람에게 짧게 인사하기", "리포트 열람", "2026.08.20")
    )
)