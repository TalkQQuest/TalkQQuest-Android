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

// 🚨 주의: 만약 다른 ViewModel(ex: ArchiveSearchViewModel) 파일에
// ActivityType과 RecentActivity가 이미 선언되어 있다면 중복 에러가 발생할 수 있습니다.
// 그럴 경우 여기 있는 선언은 지우고 기존 파일을 import 해서 사용해 주세요!
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
        loadArchiveHomeData()
    }

    fun loadArchiveHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = archiveRepository.getArchiveSummary()) {
                is ApiResult.Success -> {
                    val summary = result.data

                    // 💡 핵심: Repository에서 전체 미션 원본 데이터를 가져옵니다.
                    val allMissions = archiveRepository.getArchiveMissions()

                    val uiActivities = summary.recentActivities.map { dto ->
                        val isMission = dto.type.uppercase() == "MISSION"

                        // 💡 타입이 '미션'일 경우에만 ID를 비교하여 원본 부가 데이터를 찾아옵니다.
                        val matchedMission = if (isMission) {
                            allMissions.find { it.id.toString() == dto.id }
                        } else null

                        RecentActivity(
                            id = dto.id,
                            type = mapToActivityType(dto.type),
                            title = dto.title,
                            status = dto.status,
                            date = dto.date,
                            // 매칭된 미션 데이터가 있다면 그 값을, 없다면 null을 안전하게 주입합니다.
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