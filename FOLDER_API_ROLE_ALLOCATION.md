# 담당 분담 — 내 폴더와 API

**내가 어느 폴더를 만지고, 어떤 API를 호출하는지**를 정리한 문서입니다.
화면 목록과 이동 경로는 [`NAVIGATION.md`](NAVIGATION.md), 코드 규칙은 [`CONVENTIONS.md`](CONVENTIONS.md)를 보세요.

아래 API 목록은 **앱 코드에 실제로 선언된 엔드포인트 전량(73개)** 입니다.
출처는 `feature/*/data/*Api.kt` 의 Retrofit 인터페이스이고, 이 문서와 코드가 다르면 코드가 맞습니다.

---

## 서버 공통

| 항목 | 값 |
| --- | --- |
| Base URL | `https://talkqquest.shop/` |
| 경로 접두사 | 모든 엔드포인트가 `api/v1/…` 로 시작 |
| 인증 | `AuthInterceptor`가 액세스 토큰을 자동으로 붙임. 401이면 `TokenRefreshClient`가 리프레시 후 1회 재시도 |
| 응답 래퍼 | `ApiResponse<T>` (`success` / `message` / `data` / `errorCode`) → `ApiResult`로 변환해 화면에 전달 |
| API 문서 | `https://talkqquest.shop/docs` (Swagger) |

아래 표의 경로는 **`api/v1` 접두사를 생략**하고 적었습니다.

---

## 기능 ID(A101, H101 …)가 뭔가요?

**기능명세서**는 앱 기능을 나열한 문서인데, 기능마다 **번호(ID)** 를 붙여놨습니다. 도서관 청구기호 같은 거예요.

```
알파벳 = 기능 묶음(카테고리)      숫자 = 그 안의 개별 기능
   H101  →  H(커뮤니티) 그룹의 101번 기능
```

알파벳 구분: `A 인증 · B 온보딩 · C 홈/미션 · D 대화 · E 피드백/리포트 · F 아카이브 · G 프로필 · H 커뮤니티 · I 모임 만들기 · J 리워드/알림`

기능명세서에서 이 번호를 찾으면 요청/응답 필드·에러코드 같은 상세가 적혀 있습니다.
**다만 실제로 붙어 있는 API는 아래 표가 기준입니다** — 명세서와 실서버가 다른 곳이 있어, 구현은 Swagger를 따랐습니다.

> **역할 분담 갱신(2026-07)**: 커뮤니티가 부가 기능이라 피그마 디자인이 뒤로 밀리면서 재분배했습니다 — **아카이브를 A→C로, 성장 리포트를 C→B로** 옮겼습니다.

---

## A담당 (지니/전준호) — 진입 · 프로필 (화면 27개)

**건드릴 폴더**
```
feature/auth/        (스플래시·로그인·회원가입·이메일 인증·약관 동의)
feature/onboarding/  (환영·성향·상황·목표·완료)
feature/profile/     (프로필·뱃지·설정·내 정보·약관·고객센터·탈퇴)
```

**기능명세서 번호**: 인증 `A101~A103` · 온보딩 `B101~B103` · 프로필/설정 `G101~G103`

**API — `feature/auth/data/AuthApi.kt` (11개)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| POST | `/auth/login` | `loginWithEmail` | 이메일 로그인 |
| POST | `/auth/oauth/kakao` | `loginWithKakao` | 카카오 로그인 |
| POST | `/auth/oauth/naver` | `loginWithNaver` | 네이버 로그인 |
| POST | `/auth/email/request` | `requestEmailCode` | 이메일 인증번호 전송 |
| POST | `/auth/email/verify` | `verifyEmailCode` | 인증번호 확인 |
| POST | `/auth/signup` | `signupWithEmail` | 이메일 회원가입 |
| POST | `/auth/refresh` | `refreshAccessToken` | 토큰 갱신 |
| POST | `/auth/logout` | `logout` | 로그아웃 |
| POST | `/users/me` | `withdraw` | 회원 탈퇴 |
| PATCH | `/users/me/onboarding` | `saveOnboardingStep` | 온보딩 단계별 저장 |
| POST | `/users/me/onboarding/complete` | `completeOnboarding` | 온보딩 완료 |

**API — 프로필이 쓰는 것**
프로필은 전용 Api 파일이 없습니다. `ProfileRepository`가 **`HomeApi`와 `NotificationApi`를 가져다 씁니다.**

| 메서드 | 경로 | 함수 | 파일 | 쓰이는 곳 |
| --- | --- | --- | --- | --- |
| GET | `/users/me` | `getMe` | HomeApi | 내 정보 |
| GET | `/users/me/dashboard` | `getMyPageDashboard` | HomeApi | 프로필 대시보드(레벨·뱃지·주간 미션) |
| PATCH | `/users/me` | `updateMe` | HomeApi | 닉네임·프로필 수정 |
| POST | `/users/me/password/verify` | `verifyPassword` | HomeApi | 비밀번호 확인 |
| PATCH | `/users/me/password` | `changePassword` | HomeApi | 비밀번호 변경 |
| GET | `/badges/me` | `getMyBadges` | HomeApi | 뱃지 컬렉션 |
| GET | `/legal/terms` | `getServiceTerms` | HomeApi | 서비스 이용약관 |
| GET | `/legal/privacy` | `getPrivacyPolicy` | HomeApi | 개인정보 처리방침 |
| GET | `/users/me/settings` | `getUserSettings` | HomeApi | 설정 조회 |
| PATCH | `/users/me/settings` | `updateUserSettings` | HomeApi | 설정 변경 |
| POST | `/uploads/profile-image` | `uploadProfileImage` | HomeApi | 프로필 이미지 업로드 |
| GET | `/notifications/settings` | `getSettings` | NotificationApi | 알림 설정 조회 |
| PATCH | `/notifications/settings` | `updateSettings` | NotificationApi | 알림 설정 변경 |

> 프로필 API가 `HomeApi`에 모여 있는 건 `/users/me` 계열을 홈과 프로필이 같이 쓰기 때문입니다.
> **프로필 전용 엔드포인트를 새로 추가한다면 `feature/profile/data/ProfileApi.kt`를 만드는 편이 낫습니다.** 지금 구조를 바꿀 필요는 없지만, HomeApi가 더 커지는 건 피하세요.

---

## B담당 (이도/윤기수) — 홈 · 알림 · 미션 · AI 대화 · 리포트 (화면 14개)

**건드릴 폴더**
```
feature/home/          (홈 대시보드, 주간 리포트 도착 모달)
feature/notification/  (알림 목록·읽음·삭제·알림 설정·FCM 토큰 등록)
feature/mission/       (미션 목록·상세·대화 설정 4단계·대화 진행·미션 완료/XP·AI 피드백 요약/상세)
feature/report/        (성장 리포트, 주간 비교 리포트, 리포트 저장 시트, 티어 승급 모션)
```

**기능명세서 번호**: 홈/미션 `C101~C103` · 대화 `D101~D103` · AI 피드백 `E101` · 리포트 `E102` · 알림 `J101·J103`

**API — `feature/home/data/HomeApi.kt` (홈이 쓰는 것)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| GET | `/home/summary` | `getHomeSummary` | 홈 대시보드 전체(레벨·XP·오늘의 미션·티어·새 주간 리포트 여부) |
| GET | `/archives/summary` | `getArchiveSummary` | 홈의 보관함 요약 |

> 같은 파일의 나머지 엔드포인트는 위 A담당(프로필) 표를 보세요. **한 파일을 두 파트가 나눠 씁니다.**

**API — `feature/mission/data/MissionApi.kt` (21개)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| GET | `/missions` | `getMissions` | 미션 목록 |
| GET | `/missions/today` | `getTodayMission` | 오늘의 미션 |
| GET | `/missions/{missionId}` | `getMissionDetail` | 미션 상세 |
| GET | `/missions/{missionId}/prep` | `getMissionPrep` | 미션 준비 정보(핵심 표현·주의할 점) |
| POST | `/missions/{missionId}/save` | `saveMission` | 미션 저장(북마크) |
| DELETE | `/missions/{missionId}/save` | `unsaveMission` | 미션 저장 해제 |
| POST | `/missions/{missionId}/complete` | `completeMission` | 미션 완료 처리 |
| POST | `/missions/{missionId}/setups` | `createMissionSetup` | 대화 설정 6축 저장(장소·상대·성별·나이·친밀도·말투) |
| POST | `/conversations` | `createConversation` | 대화 생성 |
| POST | `/conversations/{conversationId}/messages` | `sendConversationMessage` | 메시지 전송 |
| GET | `/conversations/{conversationId}/suggestions` | `getConversationSuggestions` | 추천 답변 |
| POST | `/conversations/{conversationId}/finish` | `finishConversation` | 대화 종료 |
| GET | `/conversations/{conversationId}/guide` | `getConversationGuide` | 대화 가이드 |
| GET | `/conversations/{conversationId}` | `getConversation` | 대화 조회 |
| GET | `/xp/summary` | `getXpSummary` | XP 요약 |
| POST | `/feedback` | `createFeedback` | 피드백 생성 요청(비동기, `pending` → `ready`) |
| GET | `/feedback/{feedbackId}` | `getFeedbackDetail` | 피드백 상세(4축 점수 + 잘한 점·개선할 점·베스트 문장) |
| POST | `/feedback/{feedbackId}/retry` | `retryFeedback` | 피드백 생성 재시도 |
| POST | `/archives/phrases` | `savePhrase` | 문장 저장 (피드백 상세의 저장 시트) |
| DELETE | `/archives/phrases/{phraseId}` | `deletePhrase` | 문장 저장 해제 |
| GET | `/archives` | `getSavedPhrases` | 저장한 문장 조회 |

**API — `feature/notification/data/NotificationApi.kt` (8개)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| GET | `/notifications` | `getNotifications` | 알림 목록 |
| PATCH | `/notifications/{notificationId}/read` | `markRead` | 알림 하나 읽음 |
| PATCH | `/notifications/all/read` | `markAllRead` | 전체 읽음 |
| DELETE | `/notifications/{notificationId}` | `deleteNotification` | 알림 하나 삭제 |
| DELETE | `/notifications` | `deleteAllNotifications` | 전체 삭제 |
| GET | `/notifications/settings` | `getSettings` | 알림 설정 조회 (프로필도 사용) |
| PATCH | `/notifications/settings` | `updateSettings` | 알림 설정 변경 (프로필도 사용) |
| POST | `/devices/fcm-token` | `registerFcmToken` | FCM 토큰 등록 |

**API — `feature/report/data/ReportApi.kt` (8개)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| GET | `/reports/growth` | `getGrowth` | 성장 리포트(4축 누적 점수 → 마름모·별·티어 계산 근거) |
| GET | `/reports/weekly-compare` | `getWeeklyCompareList` | 주간 비교 리포트 목록(주차 이동에 사용) |
| GET | `/reports/weekly-compare/{reportId}` | `getWeeklyCompareDetail` | 주간 비교 리포트 상세 |
| POST | `/reports/weekly-compare/{reportId}/save` | `saveWeeklyCompare` | 주간 비교 리포트 보관함 저장 |
| DELETE | `/reports/weekly-compare/{reportId}` | `unsaveWeeklyCompare` | 주간 비교 리포트 저장 해제 |
| POST | `/reports` | `saveReport` | 성장 리포트 보관함 저장 (`conversationId` 전달) |
| DELETE | `/reports/{reportId}` | `deleteReport` | 성장 리포트 저장 해제 |
| GET | `/reports` | `getSavedReports` | 저장한 리포트 목록 |

**B의 리포트 담당 범위**
- **성장 리포트(`ReportScreen`) 화면 자체 + "리포트 저장하기" 저장 시트 + 티어 승급 모션**
- **주간 비교 리포트(`WeeklyCompareScreen`) 화면 자체**
- 아카이브에서 저장된 리포트를 여는 화면(`ArchiveReportScreen`, `ArchiveWeeklyCompareReportScreen`)은 **C담당**입니다.

**티어·별 계산은 앱에서 합니다** (`core/util/TierProgress.kt`).
서버는 4축 누적 점수만 주고, 축당 300점 · 4축을 모두 채우면 마름모 1개 = 별 1개 · 별 3개면 티어 승급이라는 규칙은 앱이 계산합니다.

---

## C담당 (훈/김재훈) — 아카이브 (화면 7개)

**건드릴 폴더**
```
feature/archive/    (아카이브 홈·검색·보관함 목록·대화 기록 상세·저장 문장 상세·보관함 리포트 2종)
```

**기능명세서 번호**: 아카이브 `F101~F103`
(커뮤니티 `H101~H102` · 모임 만들기 `I101~I103`은 구현 범위에서 제외됐습니다.)

**API — `feature/archive/data/ArchiveApi.kt` (12개)**

| 메서드 | 경로 | 함수 | 쓰이는 곳 |
| --- | --- | --- | --- |
| GET | `/archives/summary` | `getArchiveSummary` | 아카이브 홈 요약 |
| GET | `/archives` | `searchArchives` | 보관함 목록·검색 (`type=mission\|conversation\|phrase\|report`) |
| GET | `/archives/conversations/{conversationId}` | `getConversationDetail` | 대화 기록 상세 |
| GET | `/archives/phrases/{phraseId}` | `getPhraseDetail` | 저장한 문장 상세 |
| GET | `/reports/{reportId}` | `getReportDetail` | 보관함 성장 리포트 상세 |
| GET | `/reports/weekly-compare/{weeklyCompareReportId}` | `getWeeklyCompareReportDetail` | 보관함 주간 비교 리포트 상세 |
| POST | `/missions/{missionId}/save` | `saveMissionArchive` | 미션 저장 |
| DELETE | `/missions/{missionId}/save` | `deleteMissionArchive` | 미션 저장 해제 |
| POST | `/archives/phrases` | `savePhraseArchive` | 문장 저장 |
| DELETE | `/archives/phrases/{phraseId}` | `deletePhraseArchive` | 문장 저장 해제 |
| POST | `/reports` | `saveReportArchive` | 리포트 저장 |
| DELETE | `/reports/{reportId}` | `deleteReportArchive` | 리포트 저장 해제 |

**보관함 목록에서 항목을 열 때**
- 미션 항목은 **B의 `MissionDetailScreen`으로 들어갑니다.** 아카이브 전용 미션 상세 화면은 만들지 않았습니다.
- 리포트 항목은 주간 여부(`isWeeklyCompare`)로 두 화면 중 하나로 갈라집니다.

**커뮤니티(모임)는 구현하지 않기로 확정했습니다.**
부가 기능이라 디자인이 뒤로 밀렸고, 남은 일정을 보관함과 리포트 완성도에 쓰기로 했습니다.
패키지·화면·route·API 어느 것도 만들지 않았고, 팝업 4종(이탈·게시 완료·탈퇴 ×2)도 함께 제외됩니다.

---

## 파트가 겹치는 API — 주의할 것

같은 엔드포인트가 두 파일에 각각 선언돼 있습니다. **한쪽을 고치면 다른 쪽도 확인하세요.**

| 엔드포인트 | 선언된 파일 | 이유 |
| --- | --- | --- |
| `POST` / `DELETE` `/missions/{missionId}/save` | MissionApi(B) · ArchiveApi(C) | 저장 시트(B)와 보관함 목록(C) 양쪽에서 저장/해제 |
| `POST` / `DELETE` `/archives/phrases`, `/archives/phrases/{id}` | MissionApi(B) · ArchiveApi(C) | 피드백 상세의 문장 저장(B)과 보관함(C) |
| `POST` / `DELETE` `/reports`, `/reports/{id}` | ReportApi(B) · ArchiveApi(C) | 리포트 저장 시트(B)와 보관함(C) |
| `GET /archives` | MissionApi(B) · ArchiveApi(C) | 저장한 문장 조회(B)와 보관함 검색(C) |
| `GET /archives/summary` | HomeApi(B) · ArchiveApi(C) | 홈 요약(B)과 아카이브 홈(C) |
| `GET` / `PATCH` `/notifications/settings` | NotificationApi 한 곳 | 알림 화면(B)과 프로필 설정(A)이 같은 함수를 씀 |

---

## 폴더가 담당과 1:1이 아닌 곳

| 사실 | 설명 |
| --- | --- |
| **`conversation` 패키지가 없음** | 대화 진행·대화 설정 화면은 전부 `feature/mission/` 안에 있습니다. |
| `feature/profile/` 에 **Api 파일 없음** | `ProfileRepository`가 `HomeApi`·`NotificationApi`를 사용합니다. |
| `feature/onboarding/` 에 **Api·ViewModel 없음** | 온보딩 화면이 `AuthApi`와 `AuthViewModel`을 사용합니다. |
| `core/util/TierProgress.kt` | 티어·별 계산(B가 씀). 서버가 주는 값이 아니라 앱 계산입니다. |
| `core/push/` | FCM 수신·토큰 등록(B). |
