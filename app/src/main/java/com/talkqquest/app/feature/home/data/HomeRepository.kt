package com.talkqquest.app.feature.home.data

import com.talkqquest.app.core.network.ApiResult
import com.talkqquest.app.core.network.safeApiCall
import com.talkqquest.app.feature.home.data.model.HomeSummary
import javax.inject.Inject

// 홈 Repository (예시). ViewModel과 API 사이를 잇는 계층.
// 각 기능은 이 패턴대로 자기 Repository를 만들면 됩니다.
// - API를 safeApiCall로 감싸 ApiResult(성공/실패/예외)로 변환해 ViewModel에 넘김.
// - @Inject constructor: Hilt가 HomeApi를 자동으로 넣어줌(HomeModule에서 제공).
class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
) {
    suspend fun getHomeSummary(): ApiResult<HomeSummary> =
        safeApiCall { homeApi.getHomeSummary() }
}
