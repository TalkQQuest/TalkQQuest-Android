# 톡깨 (TalkQQuest)

AI가 추천하는 현실 대화 미션을 수행하고, 기록과 성장 리포트로 사회적 자신감의 변화를 확인하는 안드로이드 앱입니다.

> 낯가림이 있는 새내기·복학생·사회초년생이 실제 상황에서 대화를 먼저 시도해볼 수 있도록, AI가 상황에 맞는 작은 대화 미션을 제안하고 그 경험을 기록으로 남겨줍니다.

---

## 팀원 소개 및 역할 분담

| 담당 | 팀원 (별명/실명) | 담당 화면 그룹 | 화면 수 |
| --- | --- | --- | --- |
| A | 지니/전준호 | 진입(스플래시·로그인·회원가입·온보딩), 프로필 | 12 |
| B | 이도/윤기수 | 미션(홈·알림·미션목록·미션상세·대화준비), AI 대화(대화진행·미션완료·XP·피드백), 성장 리포트(성장·주간 비교 + 저장 시트) | 12 |
| C | 훈/김재훈 | 아카이브(보관함에서 진입하는 상세 화면 포함), 커뮤니티(모임 검색·생성·참여·내 모임) | 14 |

> 화면 수는 초기 시안 기준 집계값입니다(전체화면 프레임 54개 → 논리 화면 35개 + 팝업 4개). 같은 화면의 상태 변형(예: 미션 상세 2개, 대화 진행 2개)은 하나의 화면으로 구현하기 때문에 프레임 수보다 적습니다. **현재 디자인 기준은 최신 시안이고 전체화면 프레임은 87개로 늘었으며, 논리 화면 재집계는 아직입니다.** 자세한 매핑은 [`NAVIGATION.md`](NAVIGATION.md) 참고.
>
> **역할 분담 갱신(2026-07)**: 커뮤니티가 부가 기능이라 피그마 디자인이 뒤로 밀리면서 재분배했습니다 — 아카이브를 A→C로, 성장 리포트를 C→B로 옮겼습니다.

---

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 빌드 설정 | Kotlin DSL (`build.gradle.kts`) |
| 아키텍처 | MVVM |
| 네트워크 | Retrofit2 + kotlinx.serialization |
| 이미지 로딩 | Coil |
| 로컬 저장소 | DataStore |
| 비동기 처리 | Coroutines / Flow |
| DI | Hilt |
| 내비게이션 | Compose Navigation |
| 소셜 로그인 | 카카오 SDK, 네이버 SDK |
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 |
| JDK | 17 |
| Android Studio | 최신 안정 버전(Ladybug 이후) |

> minSdk 26, targetSdk 36은 2026년 7월 기준 구글플레이 정책(2026.08.31까지 API 36 타겟 필수)에 맞춘 표준값입니다. 팀 상황에 따라 조정 가능하면 회의에서 변경하세요.

**백엔드**: Node.js 20 + Express + TypeScript + Prisma ORM + MySQL + JWT/OAuth2.0

---

## 프로젝트 폴더 구조

세부 규칙과 각 폴더의 역할은 [`CONVENTIONS.md`](CONVENTIONS.md) 참고.

```
com.talkqquest.app
├── core/                  # 여러 화면이 공통으로 쓰는 것
│   ├── network/           # Retrofit, Interceptor, ApiResult
│   ├── datastore/         # 토큰, 유저 설정 로컬 저장
│   ├── designsystem/      # 공통 컴포넌트(버튼, 카드, 바텀네비 등), 컬러/타이포
│   ├── di/               # 앱 전역 Hilt 모듈
│   └── util/              # 공통 확장함수, 상수
├── feature/
│   ├── auth/              # 스플래시, 로그인, 회원가입
│   ├── onboarding/        # 온보딩 성향 입력
│   ├── home/              # 홈
│   ├── notification/      # 알림(홈 종 버튼)
│   ├── mission/           # 미션 목록/상세/저장 시트
│   ├── conversation/      # 대화 진행/완료/피드백
│   ├── archive/           # 아카이브
│   ├── community/         # 커뮤니티/모임
│   ├── report/            # 성장 리포트
│   └── profile/           # 프로필
└── navigation/            # NavGraph, Screen route 정의
```

---

## 시작 가이드

**clone 후, 내가 무엇을 맡는지부터 확인하세요.** 담당자별로 어떤 패키지(폴더)를 건드리고, 기능명세서 어느 부분(API)을 봐야 하는지는 [`FOLDER_API_ROLE_ALLOCATION.md`](FOLDER_API_ROLE_ALLOCATION.md)에 정리되어 있습니다.

> 📌 **이 문서는 이럴 때 봅니다**: "내가 어느 폴더에서 작업하지?", "내 담당 화면의 API는 기능명세서 어디서 찾지?" 싶을 때. 작업 시작 전에 자기 담당 부분만 확인하면 됩니다.

## 컨벤션 문서

브랜치, 커밋, PR, 코드 네이밍, 패키지 구조, 스크린 네이밍 규칙 전체는 [`CONVENTIONS.md`](CONVENTIONS.md)에 정리되어 있습니다. **작업 시작 전에 꼭 한 번 읽어주세요.**

---

## 빌드 및 실행 방법

1. Android Studio(최신 안정 버전) 설치
2. JDK 17 설치 확인
3. 레포 클론 후 Android Studio에서 열기
4. `local.properties`에 필요한 키 추가 (카카오/네이버 SDK 키 등 — 팀 채널에서 공유 예정)
5. Gradle Sync 후 실행 (`app` 모듈, 에뮬레이터 또는 실기기 API 26 이상)

---

## 화면 목록 & 플로우

전체 화면 목록(논리 화면 35개 + 팝업 4개), 스크린 ID, 진입 경로, 와이어프레임 대응, 담당자 표와 네비게이션 플로우 다이어그램은 [`NAVIGATION.md`](NAVIGATION.md)에서 확인하세요.
