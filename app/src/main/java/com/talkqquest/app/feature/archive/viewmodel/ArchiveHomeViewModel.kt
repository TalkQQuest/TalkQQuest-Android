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
    val difficulty: String? = null,
    val category: String? = null,
    val estimatedMinutes: Int? = null,
    val rewardXp: Int? = null
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

    // 💡 날것의 ISO 시간을 yyyy.MM.dd 포맷으로 예쁘게 바꿔주는 함수 추가
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

                    // 서버에서 받은 전체 목록 중 최대 4개까지만 추출하여 UI에 반영
                    val uiActivities = summary.recentItems.take(4).map { dto ->

                        // 💡 수정됨: 빈 값이 아닌 올바른 상태 텍스트 매핑
                        val statusText = when (dto.type.lowercase()) {
                            "conversation" -> "대화 완료"
                            "phrase", "sentence" -> "문장 저장"
                            "report" -> "리포트 열람"
                            else -> ""
                        }

                        RecentActivity(
                            id = dto.id,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = statusText, // 💡 수정됨: 상태 텍스트 노출
                            date = formatIsoDate(dto.createdAt), // 💡 수정됨: 날짜 포맷팅 적용
                            difficulty = dto.difficulty,
                            category = dto.category,
                            estimatedMinutes = dto.estimatedMinutes,
                            rewardXp = dto.rewardXp
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