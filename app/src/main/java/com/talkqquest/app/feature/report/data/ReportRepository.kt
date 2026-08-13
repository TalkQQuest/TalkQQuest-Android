package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.core.util.toSavedDate
import com.talkqquest.app.feature.report.data.model.Competency
import com.talkqquest.app.feature.report.data.model.CompetencyAxis
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.toGrowthTierReport
import javax.inject.Inject
import javax.inject.Singleton

// 리포트 Repository (미션/홈 패턴과 동일한 계층).
// 서버 우선(GET /reports/*) + 실패/데모(USE_MOCK)면 목업 폴백 (미션·알림과 동일 구조).
@Singleton
class ReportRepository @Inject constructor(
    private val reportApi: ReportApi,
) {

    // 성장 리포트(B) — GET /api/v1/reports/growth의 growthTotals(누적 원값 4개)로 티어·별·마름모를 계산.
    // gains는 마름모 꼭짓점 "+N" — 서버에 증가분 필드가 없어 피드백 화면이 방금 받은 점수를 넘겨준다.
    // 실패/데모(USE_MOCK)면 stub 폴백(화면 공백 방지 — 다른 리포트 조회와 같은 방식).
    suspend fun getGrowthReport(gains: List<Int> = emptyList()): ApiResult<GrowthTierReport> {
        val r = serverCall { reportApi.getGrowth() }
        return if (r is ApiResult.Success) ApiResult.Success(r.data.toGrowthTierReport(gains))
        else ApiResult.Success(stubGrowth)
    }

    // 리포트 저장 (리포트 저장 시트) — POST /api/v1/reports.
    // 2026-08-10 백엔드 변경으로 바디가 type → conversationId. 성장 리포트 전용이 됐고,
    // 주간 비교는 별도 API(POST /reports/weekly-compare/{id}/save)로 갈라졌다.
    // conversationId가 비면 서버가 400을 주므로 호출하지 않고 실패로 돌려준다(화면은 낙관적 표시 유지).
    suspend fun saveReport(conversationId: String): ApiResult<SaveReportResponse> {
        if (conversationId.isBlank()) {
            return ApiResult.Error(code = null, message = "저장할 대화를 찾지 못했어요.")
        }
        return serverCall { reportApi.saveReport(SaveReportRequest(conversationId = conversationId)) }
    }

    // 리포트 저장 해제 — DELETE /api/v1/reports/{reportId}.
    // 저장 응답에서 받은 서버 id로만 의미가 있고, 실패/데모면 조용히 무시(화면은 낙관적 표시 유지).
    suspend fun deleteReport(reportId: String): ApiResult<DeleteReportResponse> =
        serverCall { reportApi.deleteReport(reportId) }

    // 저장한 리포트 목록 (리포트 저장 시트의 "최근 저장한 리포트") — GET /api/v1/reports.
    // 서버가 페이지 파라미터를 안 받아 최신 size개만 잘라 쓴다(응답은 최신순 가정 — 아니어도 날짜로 정렬).
    // 실패/데모(USE_MOCK)면 Error를 그대로 돌려줘 호출부가 기존 목업을 유지하게 한다.
    suspend fun getSavedReports(size: Int = 5): ApiResult<List<SavedReportItem>> =
        when (val r = serverCall { reportApi.getSavedReports() }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.reports
                    .sortedByDescending { it.createdAt }
                    .take(size)
                    .map {
                        SavedReportItem(
                            id = it.id,
                            title = it.title,
                            savedDate = it.createdAt.toSavedDate(),
                            type = it.type,
                        )
                    },
            )
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }

    // 오프라인/데모(USE_MOCK) 폴백 — UI CSS 목업 값 그대로. 서버가 응답하면 쓰이지 않는다.
    private val stubGrowth = GrowthTierReport(
        tierName = "골드",
        tierStars = 2,
        nextStarsNeeded = 1,
        nextTierName = "플래티넘",
        competencies = listOf(
            Competency(CompetencyAxis.KINDNESS, "친절한 태도", "친절한 태도", score = 300, gain = 70),
            Competency(CompetencyAxis.INITIATIVE, "대화 주도", "대화 주도", score = 200, gain = 70),
            Competency(CompetencyAxis.EMPATHY, "공감 능력", "공감 능력", score = 100, gain = 70),
            Competency(CompetencyAxis.QUESTION_LINK, "질문 연결성", "질문 연결성", score = 300, gain = 70),
        ),
    )

}
