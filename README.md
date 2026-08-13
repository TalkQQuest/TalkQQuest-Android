# 톡깨 (TalkQQuest)

AI가 추천하는 현실 대화 미션을 수행하고, 기록과 성장 리포트로 사회적 자신감의 변화를 확인하는 안드로이드 앱입니다.

> 낯가림이 있는 새내기·복학생·사회초년생이 실제 상황에서 대화를 먼저 시도해볼 수 있도록, AI가 상황에 맞는 작은 대화 미션을 제안하고 그 경험을 기록으로 남겨줍니다.

---

## 어떻게 동작하나

```
오늘의 미션 추천  →  대화 설정(장소·상대·성별·나이·친밀도·말투)  →  AI와 대화 연습
      →  미션 완료 · XP 획득  →  AI 피드백(4축 점수)  →  성장 리포트(마름모·별·티어)
      →  보관함에 저장 (미션 · 대화 · 문장 · 리포트)
```

- **AI 피드백 4축**: 친절한 태도 · 대화 주도 · 공감 능력 · 질문 연결성. 축마다 점수와 잘한 점·개선할 점·베스트 문장을 줍니다.
- **성장 리포트**: 4축 누적 점수를 마름모로 그립니다. 축당 300점씩 4축을 모두 채우면 마름모 하나가 완성되고 **별 1개**, 별 3개를 모으면 **티어 승급**(브론즈 → 실버 → 골드 → 플래티넘 → 다이아 → 마스터)입니다.
- **주간 비교 리포트**: 지난주와 이번 주를 비교합니다. 주가 끝나면 알림으로 안내되고, 홈에 도착 모달이 뜹니다.

---

## 팀원 소개 및 역할 분담

| 담당 | 팀원 (별명/실명) | 담당 화면 그룹 | 구현된 화면 수 |
| --- | --- | --- | --- |
| A | 지니/전준호 | 진입(스플래시·로그인·회원가입·온보딩), 프로필(설정·뱃지·약관·탈퇴) | 27 |
| B | 이도/윤기수 | 홈·알림, 미션(목록·상세·대화 설정 4단계), AI 대화(대화 진행·미션 완료/XP·피드백), 리포트(성장·주간 비교 + 저장 시트) | 14 |
| C | 훈/김재훈 | 아카이브(홈·검색·보관함 목록·상세 4종), 커뮤니티(미구현) | 7 |

> **역할 분담 갱신(2026-07)**: 커뮤니티가 부가 기능이라 피그마 디자인이 뒤로 밀리면서 재분배했습니다 — 아카이브를 A→C로, 성장 리포트를 C→B로 옮겼습니다.
>
> 화면 수는 **코드에 실제로 등록된 화면 기준**입니다. 화면 이름·route·진입 경로 전체는 [`NAVIGATION.md`](NAVIGATION.md)를 보세요.
> 커뮤니티 화면 7종은 아직 만들지 않았습니다(`community_list` route가 자리표시 화면만 그림).

---

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material3) |
| 빌드 설정 | Kotlin DSL (`build.gradle.kts`) + 버전 카탈로그(`gradle/libs.versions.toml`) |
| 아키텍처 | MVVM + Repository |
| 네트워크 | Retrofit 3 + OkHttp 5 + kotlinx.serialization |
| 이미지 로딩 | Coil 3 |
| 로컬 저장소 | DataStore Preferences |
| 비동기 처리 | Coroutines / Flow |
| DI | Hilt (KSP) |
| 내비게이션 | Navigation Compose |
| 푸시 | Firebase Cloud Messaging |
| 블러 효과 | haze (하단 네비게이션 유리 효과) |
| 소셜 로그인 | 카카오 SDK, 네이버 SDK |
| minSdk / targetSdk / compileSdk | 26 / 36 / **36.1** |
| AGP / Gradle / JDK | 9.1.1 / 9.3.1 / 17 |

> compileSdk는 `release(36) { minorApiLevel = 1 }` 로 지정돼 있습니다(= API 36.1).
> **버전을 임의로 올리지 마세요.** 라이브러리별 고정 사유는 [`CONVENTIONS.md`](CONVENTIONS.md) "7. 라이브러리 버전"에 정리돼 있습니다.

**백엔드**: Node.js + Express + TypeScript + Prisma ORM + MySQL + JWT/OAuth2.0
**API 서버**: `https://talkqquest.shop/` · **API 문서**: `https://talkqquest.shop/docs` (Swagger)

---

## 프로젝트 폴더 구조

세부 규칙과 각 폴더의 역할은 [`CONVENTIONS.md`](CONVENTIONS.md), 담당·API 배분은 [`FOLDER_API_ROLE_ALLOCATION.md`](FOLDER_API_ROLE_ALLOCATION.md) 참고.

```
com.talkqquest.app
├── core/                  # 여러 화면이 공통으로 쓰는 것
│   ├── network/           # ApiResponse/ApiResult, AuthInterceptor, TokenRefreshClient
│   ├── datastore/         # 토큰, 알림 상태, XP 로컬 저장
│   ├── designsystem/      # 컬러·타이포·테마 + 공통 컴포넌트(component/)
│   ├── di/                # 앱 전역 Hilt 모듈 (NetworkModule)
│   ├── push/              # FCM 수신·토큰 등록
│   └── util/              # 날짜 포맷, 티어 계산(TierProgress)
├── feature/
│   ├── auth/              # 스플래시, 로그인, 회원가입, 약관 동의        (A)
│   ├── onboarding/        # 환영·성향·상황·목표·완료                    (A)
│   ├── profile/           # 프로필, 설정, 뱃지, 약관, 탈퇴              (A)
│   ├── home/              # 홈 대시보드, 주간 리포트 도착 모달           (B)
│   ├── notification/      # 알림 목록·설정·FCM                         (B)
│   ├── mission/           # 미션·대화 설정 4단계·대화 진행·완료·피드백    (B)
│   ├── report/            # 성장 리포트, 주간 비교 리포트, 티어 승급 모션 (B)
│   ├── archive/           # 아카이브 홈·검색·보관함 목록·상세            (C)
│   ├── community/         # 커뮤니티 — 아직 비어 있음                   (C)
│   └── conversation/      # 비어 있음 (대화 화면은 mission/ 안에 있음)
└── navigation/
    ├── MainScreen.kt      # 앱 최상위 셸 (NavGraph + 하단 바 + 전역 모달)
    ├── NavGraph.kt        # 전체 네비게이션 그래프
    ├── MainTabsPager.kt   # 하단 4탭 페이저 (홈·미션·아카이브·프로필)
    ├── BottomNavItem.kt   # 하단 탭 정의
    ├── TqBottomBar.kt     # 하단 네비게이션 바
    └── Screen.kt          # 화면 route 상수 정의
```

각 feature는 안에서 `ui/` · `viewmodel/` · `data/`(+`data/model/`) · `di/` 로 나뉩니다.

---

## 빌드 및 실행 방법

1. Android Studio 최신 안정 버전 설치 (AGP 9.1.1을 지원하는 버전)
2. JDK 17 설치 확인
3. 레포 클론 후 Android Studio에서 열기
4. **`local.properties`에 소셜 로그인 키 추가** (팀 채널에서 공유)
   ```properties
   KAKAO_NATIVE_APP_KEY=...
   NAVER_CLIENT_ID=...
   NAVER_CLIENT_SECRET=...
   NAVER_CLIENT_NAME=TalkQQuest
   ```
   `local.properties`는 git에 올라가지 않습니다. 키가 없어도 빌드는 되지만 소셜 로그인이 동작하지 않습니다.
5. Gradle Sync 후 실행 (`app` 모듈, 에뮬레이터 또는 실기기 **API 26 이상**)

Firebase 설정 파일(`app/google-services.json`)은 레포에 포함돼 있어 따로 받을 필요가 없습니다.
카카오 SDK는 Maven Central에 없어 `settings.gradle.kts`에 전용 저장소(`devrepo.kakao.com`)가 등록돼 있습니다.

명령줄 빌드:
```
./gradlew :app:assembleDebug
```

**앱 권한**: 인터넷(`INTERNET`), 알림(`POST_NOTIFICATIONS`).

---

## 시작 가이드

**clone 후, 내가 무엇을 맡는지부터 확인하세요.**

| 이럴 때 | 볼 문서 |
| --- | --- |
| "내가 어느 폴더에서 작업하지?", "내 담당 화면의 API는 뭐지?" | [`FOLDER_API_ROLE_ALLOCATION.md`](FOLDER_API_ROLE_ALLOCATION.md) |
| "이 화면 route가 뭐지?", "이 화면엔 어디서 들어오지?" | [`NAVIGATION.md`](NAVIGATION.md) |
| "브랜치 이름·커밋 메시지·PR은 어떻게 쓰지?", "색·폰트·공통 버튼은 뭘 쓰지?" | [`CONVENTIONS.md`](CONVENTIONS.md) |

**작업 시작 전에 [`CONVENTIONS.md`](CONVENTIONS.md)를 꼭 한 번 읽어주세요.** 브랜치·커밋·PR 규칙과 디자인 토큰 사용법이 거기에 있습니다.
