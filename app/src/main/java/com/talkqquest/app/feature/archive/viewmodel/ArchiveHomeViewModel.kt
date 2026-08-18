package com.talkqquest.app.feature.archive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.archive.data.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ActivityType {
    MISSION, CONVERSATION, SENTENCE, REPORT
}

data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val status: String,
    val date: String,
    val duration: String = "", // time 대신 API 명세인 duration으로 변경
    val difficulty: String? = null,
    val category: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null,
    val tags: List<String> = emptyList(),
    val summary: String? = null,
    val reportType: String? = null,
    // 정렬 전용 원본 시각(서버 createdAt, 시각 포함 ISO 문자열). date는 yyyy.MM.dd로 시각이
    // 잘려 있어 정렬용으로 쓰면 같은 날 항목이 전부 동률이 된다. 화면 표시는 date 그대로 쓰고
    // 정렬만 이 값으로 한다. 기본값 ""이라 다른 화면의 기존 생성 호출은 안 깨진다.
    val createdAtRaw: String = ""
)

data class ArchiveHomeUiState(
    val completedMissionCount: Int = 0,
    val conversationCount: Int = 0,
    val savedSentenceCount: Int = 0,
    val reportCount: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ArchiveHomeViewModel @Inject constructor(
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveHomeUiState())
    val uiState: StateFlow<ArchiveHomeUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    private fun formatIsoDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString)
            zdt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            isoString.substringBefore("T").replace("-", ".")
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = archiveRepository.getArchiveSummary()) {
                is ApiResult.Success -> {
                    val summary = result.data

                    val uiActivities = summary.recentItems.take(4).map { dto ->

                        val statusText = when (dto.type.lowercase()) {
                            "conversation" -> "대화 완료"
                            "phrase", "sentence" -> "문장 저장"
                            "report" -> "리포트 열람"
                            else -> ""
                        }

                        val actualId = dto.referenceId ?: dto.id

                        RecentActivity(
                            id = actualId,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = statusText,
                            date = formatIsoDate(dto.createdAt),
                            duration = dto.duration ?: "", // duration으로 매핑
                            difficulty = dto.difficulty,
                            category = dto.category,
                            estimatedMinutes = dto.estimatedMinutes,
                            rewardXp = dto.rewardXp,
                            tags = dto.tags,
                            summary = dto.description,
                            reportType = dto.reportType
                        )
                    }

                    _uiState.update {
                        it.copy(
                            completedMissionCount = summary.missionRecordCount,
                            conversationCount = summary.conversationCount,
                            savedSentenceCount = summary.phraseCount,
                            reportCount = summary.reportCount,
                            recentActivities = uiActivities,
                            isLoading = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "데이터를 불러오는데 실패했습니다.")
                    }
                }
                is ApiResult.Exception -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "네트워크 오류가 발생했습니다.")
                    }
                }
            }
        }
    }

    private fun mapToActivityType(typeString: String): ActivityType {
        return when (typeString.lowercase()) {
            "mission" -> ActivityType.MISSION
            "conversation" -> ActivityType.CONVERSATION
            "phrase", "sentence" -> ActivityType.SENTENCE
            "report" -> ActivityType.REPORT
            else -> ActivityType.MISSION
        }
    }
}