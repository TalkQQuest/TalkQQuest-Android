import java.util.Properties


val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name).orEmpty()

fun String.toBuildConfigString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.talkqquest.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.talkqquest.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", localProperty("KAKAO_NATIVE_APP_KEY").toBuildConfigString())
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperty("KAKAO_NATIVE_APP_KEY")

        buildConfigField("String", "NAVER_CLIENT_ID", localProperty("NAVER_CLIENT_ID").toBuildConfigString())
        buildConfigField("String", "NAVER_CLIENT_SECRET", localProperty("NAVER_CLIENT_SECRET").toBuildConfigString())
        buildConfigField("String", "NAVER_CLIENT_NAME", localProperty("NAVER_CLIENT_NAME").ifBlank { "TalkQQuest" }.toBuildConfigString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 발표/데모용으로 릴리스를 바로 설치·구동할 수 있게 디버그 키로 서명(내부용).
            // 릴리스는 AOT 최적화 + baseline-prof.txt 적용으로 콜드스타트가 debug보다 크게 빠르다.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // DI - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Firebase (FCM 푸시)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Glassmorphism blur
    implementation(libs.haze)

    // Local storage
    implementation(libs.androidx.datastore.preferences)

    // Baseline Profile 설치기 — 릴리스 빌드에서 앱/Compose 번들 프로파일을 설치해 콜드스타트 가속.
    // (src/main/baseline-prof.txt + Compose/AndroidX 라이브러리에 동봉된 프로파일)
    implementation(libs.androidx.profileinstaller)

    // Social login
    implementation(libs.kakao.sdk.user)
    implementation(libs.naver.login.sdk)
}
