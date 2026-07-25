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

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = archiveRepository.getArchiveSummary()) {
                is ApiResult.Success -> {
                    val summary = result.data

                    // 서버에서 미션 상세 정보까지 모두 주므로, 더 이상 로컬 매칭이 필요 없습니다!
                    val uiActivities = summary.recentItems.map { dto ->
                        RecentActivity(
                            id = dto.id,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = "", // 홈 화면 디자인에서는 상태 텍스트를 띄우지 않으므로 빈 값 처리
                            date = dto.createdAt,
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