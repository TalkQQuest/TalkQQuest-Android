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

    // 💡 화면 진입 시 최신 상태를 반영할 수 있도록 public 함수로 노출
    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = archiveRepository.getArchiveSummary()) {
                is ApiResult.Success -> {
                    val summary = result.data
                    val allMissions = archiveRepository.getArchiveMissions()

                    val uiActivities = summary.recentActivities.map { dto ->
                        val isMission = dto.type.uppercase() == "MISSION"
                        val matchedMission = if (isMission) {
                            allMissions.find { it.id.toString() == dto.id }
                        } else null

                        RecentActivity(
                            id = dto.id,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = dto.status,
                            date = dto.date,
                            difficulty = matchedMission?.difficulty,
                            category = matchedMission?.category,
                            estimatedMinutes = matchedMission?.duration,
                            rewardXp = matchedMission?.xp
                        )
                    }

                    _uiState.update {
                        it.copy(
                            completedMissionCount = summary.completedMissionCount,
                            conversationCount = summary.conversationCount,
                            savedSentenceCount = summary.savedSentenceCount,
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
        return when (typeString.uppercase()) {
            "MISSION" -> ActivityType.MISSION
            "CONVERSATION" -> ActivityType.CONVERSATION
            "SENTENCE" -> ActivityType.SENTENCE
            "REPORT" -> ActivityType.REPORT
            else -> ActivityType.MISSION
        }
    }
}