plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
}

android {
    namespace = "com.exa.android.letstalk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.exa.android.letstalk"
        minSdk = 24
        targetSdk = 34
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.androidx.datastore.preferences.core.jvm)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // for window insets
    implementation("androidx.compose.foundation:foundation:1.5.0")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    // Room Database
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    // Kotlin Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")

     //Dagger2
    implementation("com.google.dagger:dagger:2.49")
    kapt("com.google.dagger:dagger-compiler:2.49")

    //Hilt
    implementation( "com.google.dagger:hilt-android:2.51.1")
    kapt ("com.google.dagger:hilt-compiler:2.51.1")

    // hilt Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // material components
    implementation("androidx.compose.material:material:1.7.5")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")

    //coil
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // Signal Protocol for E2EE
    implementation("org.whispersystems:signal-protocol-android:2.8.1")

//    //Shimmer Effect
//    implementation("com.valentinilk.shimmer:compose-shimmer:1.0.5")

    implementation("androidx.compose.compiler:compiler:1.5.3")
}