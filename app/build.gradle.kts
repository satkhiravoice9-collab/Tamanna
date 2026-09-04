plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sabbirsamol.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sabbirsamol.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.activity:activity-ktx:1.9.3")

    implementation("androidx.core:core-ktx:1.13.1")

    // PDF Viewer (অফলাইন পড়া ও রিজিউম করার জন্য)
    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    
    // Coroutines (ফাইল ডাউনলোডের জন্য)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
}
