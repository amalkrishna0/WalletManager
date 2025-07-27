plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.walletmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.walletmanager"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Glide
    implementation(libs.glide)
    kapt(libs.compiler)

    // SMS Retriever API
    implementation(libs.play.services.auth.api.phone)

    // Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation (libs.androidx.biometric)

}
