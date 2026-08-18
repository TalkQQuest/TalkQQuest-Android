package com.talkqquest.app.feature.report.data

import com.talkqquest.app.core.datastore.TierStore
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.serverCall
import com.talkqquest.app.core.util.GrowthTotalsDto
import com.talkqquest.app.core.util.toSavedDate
import com.talkqquest.app.feature.archive.data.ArchiveApi
import com.talkqquest.app.feature.report.data.model.DeleteReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthReportResponse
import com.talkqquest.app.feature.report.data.model.GrowthTierReport
import com.talkqquest.app.feature.report.data.model.SaveReportRequest
import com.talkqquest.app.feature.report.data.model.SaveReportResponse
import com.talkqquest.app.feature.report.data.model.SavedReportItem
import com.talkqquest.app.feature.report.data.model.toGrowthTierReport
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// 리포트 Repository (미션/홈 패턴과 동일한 계층).
// 서버 우선(GET /reports/*) + 실패/데모(USE_MOCK)면 목업 폴백 (미션·알림과 동일 구조).
@Singleton
class ReportRepository @Inject constructor(
    private val reportApi: ReportApi,
    // 저장 시트의 "보관함" 목록을 보관함 API에서 받는다 — 리포트 종류(reportType)를 주는 유일한 목록이라.
    private val archiveApi: ArchiveApi,
    private val tierStore: TierStore, // 승급 후 티어를 홈과 공유해 홈 복귀 첫 프레임부터 새 티어가 보이게 함
) {

    // 성장 리포트(B) — GET /api/v1/reports/growth의 growthTotals(누적 원값 4개)로 티어·별·마름모를 계산.
    // gains는 마름모 꼭짓점 "+N" — 서버에 증가분 필드가 없어 피드백 화면이 방금 받은 점수를 넘겨준다.
    // 실패하면 목업으로 덮지 않고 그대로 돌려준다 — 틀린 티어가 뜨는 것보다 오류가 뜨는 게 낫다(사용자 결정).
    suspend fun getGrowthReport(gains: List<Int> = emptyList()): ApiResult<GrowthTierReport> =
        when (val r = serverCall { reportApi.getGrowth() }) {
            is ApiResult.Success -> {
                val report = r.data.toGrowthTierReport(gains)
                // report.tierName/tierStars는 마름모를 완성한 이번 회차 기준 "승급 전" 값이다
                // (화면이 승급 전 상태로 시작해 모션으로 갈아끼우는 연출이라서). 홈에는 승급이 끝난
                // "이후" 값을 줘야 하므로, 승급이 있었던 회차(promotion != null)는 promotion의
                // newTierName/newTierStars를 쓰고, 승급이 없었던 회차만 report 값을 그대로 쓴다.
                tierStore.update(
                    tierName = report.promotion?.newTierName ?: report.tierName,
                    tierStars = report.promotion?.newTierStars ?: report.tierStars,
                )
                ApiResult.Success(report)
            }
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }

    // 보관함·저장 시트에서 특정 리포트 id로 들어온 경로(12-A 경로 통합) — 홈과 같은 계산
    // (toGrowthTierReport)을 그대로 태운다. 보관함용 계산 로직은 새로 만들지 않는다.
    // GET /api/v1/reports/{reportId}(보관함 API, ArchiveApi.getReportDetail)가 이 리포트 하나의
    // growthTotals·recentScores를 준다 — recentScores가 이 리포트로 오른 gains 자리를 대신한다.
    // 지나간 리포트를 보는 화면이라 홈의 "현재 티어" 표시(tierStore)는 건드리지 않는다.
    suspend fun getGrowthReportById(reportId: String): ApiResult<GrowthTierReport> =
        when (val r = serverCall { archiveApi.getReportDetail(reportId) }) {
            is ApiResult.Success -> {
                val totals = r.data.growth?.growthTotals
                val recent = r.data.recentScores
                val gains = listOf(
                    recent?.kindness ?: 0,
                    recent?.initiative ?: 0,
                    recent?.empathy ?: 0,
                    recent?.questionLink ?: 0,
                )
                val response = GrowthReportResponse(
                    growthTotals = GrowthTotalsDto(
                        kindnessTotal = totals?.kindnessTotal ?: 0,
                        initiativeTotal = totals?.initiativeTotal ?: 0,
                        empathyTotal = totals?.empathyTotal ?: 0,
                        questionLinkTotal = totals?.questionLinkTotal ?: 0,
                    ),
                )
                ApiResult.Success(response.toGrowthTierReport(gains))
            }
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
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

    // 저장한 리포트 목록 (리포트 저장 시트의 "보관함") — GET /api/v1/archives?type=report.
    //
    // 예전에는 GET /api/v1/reports를 썼는데 그 응답에는 항목 종류(type)가 없고 주간 비교 리포트도
    // 빠져 있었다. 종류를 모르면 카드를 눌렀을 때 성장/주간 중 어디로 보낼지 정할 수 없고,
    // 값이 비어 성장으로 판정되던 게 지금은 우연히 맞았을 뿐(주간이 목록에 없어서) 서버가 주간을
    // 포함시키는 순간 전부 엉뚱한 화면으로 갔을 자리다. 보관함 API는 reportType을 정확히 주고
    // 주간 비교도 포함해서 준다.
    //
    // 주의: 보관함 응답의 id는 "보관함 항목" id이고 실제 리포트 id는 referenceId다(실측 확인).
    // 저장 해제(DELETE /reports/{id})와 상세 이동이 리포트 id를 쓰므로 referenceId를 담아야 한다.
    //
    // 실패/데모(USE_MOCK)면 Error를 그대로 돌려줘 호출부가 기존 목업을 유지하게 한다.
    suspend fun getSavedReports(size: Int = 5): ApiResult<List<SavedReportItem>> =
        when (val r = serverCall { archiveApi.searchArchives(type = "report", size = 100) }) {
            is ApiResult.Success -> ApiResult.Success(
                r.data.items
                    .filter { it.isBookmarked }
                    .sortedByDescending { it.createdAt }
                    .take(size)
                    .map {
                        SavedReportItem(
                            id = it.referenceId ?: it.id,
                            title = it.title,
                            savedDate = it.createdAt.toSavedDate(),
                            type = it.reportType.orEmpty(),
                        )
                    }
                    .let { withWeekRangeTitles(it) },
            )
            is ApiResult.Error -> r
            is ApiResult.Exception -> r
        }

    // 주간 비교 리포트는 목록 응답에 주차 범위가 없어 제목이 "4주차 비교 리포트"로만 온다.
    // 그 항목만 상세를 한 번씩 더 불러 주차 라벨 두 개를 채운다 — 카드가 그걸로
    // "7월 4주차 →(그림) 8월 1주차 주간 비교 리포트"를 조립한다(방금 저장한 카드와 같은 모양).
    // 성장 리포트는 제목이 이미 미션명이라 호출하지 않는다.
    private suspend fun withWeekRangeTitles(items: List<SavedReportItem>): List<SavedReportItem> {
        val targets = items.filter { it.type == "weekly_compare" }
        if (targets.isEmpty()) return items

        val labels: Map<String, Pair<String, String>> = coroutineScope {
            targets.map { item ->
                async {
                    val d = serverCall { reportApi.getWeeklyCompareDetail(item.id) }
                    val period = (d as? ApiResult.Success)?.data?.periodLabel.orEmpty()
                    val parts = period.split("→").map { it.trim() }.filter { it.isNotEmpty() }
                    item.id to (parts.getOrElse(0) { "" } to parts.getOrElse(1) { "" })
                }
            }.awaitAll()
        }.filter { it.second.first.isNotBlank() && it.second.second.isNotBlank() }.toMap()

        // 상세를 못 받은 항목은 서버 제목을 그대로 둔다(라벨이 비면 카드가 title을 그린다).
        return items.map { item ->
            labels[item.id]?.let { (prev, next) ->
                item.copy(
                    title = "$prev → $next 주간 비교 리포트",
                    prevWeekLabel = prev,
                    thisWeekLabel = next,
                )
            } ?: item
        }
    }

}
