plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt") // Add Kapt plugin
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services) // Apply Hilt plugin
}

android {
    namespace = "com.example.smartfarm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smartfarm"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-gcm:11.2.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1") // Use the latest Hilt version
    kapt("com.google.dagger:hilt-android-compiler:2.51.1") // Use the latest Hilt version

    // Optional - For Hilt and ViewModel
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0") // Or latest
    // Optional - For Hilt and WorkManager
    implementation("androidx.hilt:hilt-work:1.2.0") // Or latest
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // add gson
    implementation("com.google.code.gson:gson:2.10.1")

    // pending intent
    implementation("androidx.work:work-runtime:2.7.0-alpha05")

    // add swipe to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}