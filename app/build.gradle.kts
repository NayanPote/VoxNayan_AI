plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.nayanpote.voxnayanai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nayanpote.voxnayanai"
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Core Android libraries
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.work:work-runtime:2.9.0")

    // Network libraries for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Permission handling
    implementation("com.karumi:dexter:6.2.3")

    // Animation libraries
    implementation("com.airbnb.android:lottie:6.0.0")

    implementation ("com.airbnb.android:lottie:6.4.0")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX dependencies
    val camerax_version = "1.3.0"

    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")


    // HTTP client for API calls
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")

    // JSON parsing
    implementation ("org.json:json:20230227")

    // Existing dependencies...
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.activity:activity:1.7.2")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // ML Kit dependencies (Google's free ML SDK)
    // ML Kit
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:image-labeling:17.0.8")
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:face-detection:16.1.6")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:pose-detection:18.0.0-beta4")
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta4")
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta6")

    // Volley for API requests
    implementation ("com.android.volley:volley:1.2.1")

    // Image processing
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // TensorFlow Lite for offline recognition (optional)
    implementation ("org.tensorflow:tensorflow-lite:2.13.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    // JSON processing
    implementation ("com.google.code.gson:gson:2.10.1")
}
