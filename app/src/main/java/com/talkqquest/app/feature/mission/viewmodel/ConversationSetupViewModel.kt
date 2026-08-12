package com.talkqquest.app.feature.mission.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.feature.mission.data.MissionRepository
import com.talkqquest.app.feature.mission.data.model.ConversationSetupSelection
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerAgeGroup
import com.talkqquest.app.feature.mission.data.model.MissionSetupEnvironment
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerGender
import com.talkqquest.app.feature.mission.data.model.MissionSetupPartnerRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 대화 설정 1~4단계가 함께 쓰는 ViewModel.
//
// 네 화면이 각자 destination이라 고른 값을 서로 모른다. 지금까지는 각 화면이 remember로
// 자기 안에만 들고 있다가 다음 화면으로 넘어가면 버려졌고, 서버로도 가지 않았다.
// 그래서 NavGraph에서 1단계 backStackEntry에 묶어 네 화면이 같은 인스턴스를 보게 한다
// (그 entry가 사라지는 시점 = 설정 흐름을 벗어나는 시점이라 수명도 딱 맞는다).
//
// 4단계 "대화 시작하기"에서 여섯 축을 모아 POST /missions/{id}/setups로 보낸다.
@HiltViewModel
class ConversationSetupViewModel @Inject constructor(
    private val missionRepository: MissionRepository,
) : ViewModel() {

    private val _selection = MutableStateFlow(ConversationSetupSelection())
    val selection: StateFlow<ConversationSetupSelection> = _selection.asStateFlow()

    // 저장 요청이 날아가는 동안 "대화 시작하기"를 두 번 누르지 못하게 하는 표시.
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun selectEnvironment(value: MissionSetupEnvironment) =
        _selection.update { it.copy(environment = value) }

    fun selectPartnerRole(value: MissionSetupPartnerRole) =
        _selection.update { it.copy(partnerRole = value) }

    fun selectGender(value: MissionSetupPartnerGender) =
        _selection.update { it.copy(partnerGender = value) }

    fun selectAgeGroup(value: MissionSetupPartnerAgeGroup) =
        _selection.update { it.copy(partnerAgeGroup = value) }

    fun selectIntimacy(index: Int) = _selection.update { it.copy(intimacyIndex = index) }

    fun selectFormality(index: Int) = _selection.update { it.copy(formalityIndex = index) }

    /**
     * 고른 6축을 서버에 저장하고 [onDone]으로 대화 화면에 넘긴다.
     *
     * ★저장 결과와 상관없이 항상 [onDone]을 부른다. 준비 정보는 AI가 참고하는 값이지
     *   대화의 전제가 아니다. 여기서 막으면 서버가 흔들릴 때 사용자가 대화에 못 들어간다.
     *   (4단계는 눈금이 기본값을 갖고 있어 1~3단계를 다 골랐으면 항상 전송 가능하다.)
     */
    fun saveAndStart(missionId: String, onDone: () -> Unit) {
        if (_isSaving.value) return
        val request = _selection.value.toRequest()
        if (request == null || missionId.isBlank()) {
            onDone() // 고르지 않고 넘어온 경우(뒤로 갔다 오는 등) — 저장만 건너뛴다
            return
        }
        _isSaving.value = true
        viewModelScope.launch {
            val r = missionRepository.saveMissionSetup(missionId, request)
            if (r is ApiResult.Error) {
                // 400 MISSION_SETUP_DISABLED_COMBINATION 등. 막을 이유가 없어 로그 대신 그냥 흘린다.
                // 서버가 막아둔 조합이 무엇인지 문서화돼 있지 않아 화면에서 미리 걸러낼 수도 없다.
            }
            _isSaving.value = false
            onDone()
        }
    }
}
