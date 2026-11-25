plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.room)
}

android {
    namespace = "com.paperloong.lux"
    compileSdk {
        // Compose 1.13.x(BOM 2026.11.01)的 AAR 元数据要求 compileSdk >= 37.1,使用平台 android-37.2
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "com.paperloong.lux"
        minSdk = 24
        targetSdk = 37
        // versionCode = major * 10000 + minor * 100 + patch，须始终大于 Play 后台已上传的最大值，不可复用
        versionCode = 30200
        versionName = "3.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

ksp {
    arg("room.generateKotlin", "true")
}

dependencies {

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.leakcanary.android)

    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Orbit MVI
    implementation(libs.bundles.orbit.mvi)

    // Androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}