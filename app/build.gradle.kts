import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
}

android {

//    packagingOptions {
//        pickFirst("lib/**/libdeepspeech.so")
//    }

    namespace = "com.ai.roboteacher"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.ai.roboteacher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
//        multiDexEnabled = true

//        ndk {
//            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
//        }


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

//    afterEvaluate {
//    // Disable test APKs
//    tasks.matching { it.name.contains("AndroidTest") }.configureEach {
//        enabled = false
//    }
//
//    // Disable debug variant
////    tasks.matching { it.name.contains("Debug", ignoreCase = true) }.configureEach {
////        enabled = false
////    }
//}
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(files("D:\\spectrumprogress-debug.aar"))
//    implementation(libs.vosk.android)
    implementation(files("C:\\Users\\Dipanjan Biswas\\Downloads\\circleanim-debug.aar"))
    //implementation(project(":models"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //retrofit2
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.5.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-cio:2.3.5")

    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")



    // Also required for Kotlinx serialization itself
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation ("io.noties.markwon:core:4.6.2")

    // Tables
    implementation("io.noties.markwon:ext-tables:4.6.2")
// Images (if you want image rendering too)
    implementation("io.noties.markwon:image:4.6.2")
// (Optional) For Coil / Glide integration
    implementation("io.noties.markwon:image-coil:4.6.2")
    implementation("io.noties.markwon:ext-latex:4.6.2")

    implementation("io.ktor:ktor-client-logging:2.3.12")

//    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")

    //Room DB
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")   // ✅ coroutine support
    kapt("androidx.room:room-compiler:2.6.1")

    //SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")



    //Vosk Voice Recognition
//    implementation ("com.alphacephei:vosk-android:0.3.47")
//    implementation ("net.java.dev.jna:jna:5.13.0@aar")
//
//    implementation("org.mozilla.deepspeech:libdeepspeech:0.9.2@aar")

//    implementation ("com.android.support:multidex:1.0.3")
}