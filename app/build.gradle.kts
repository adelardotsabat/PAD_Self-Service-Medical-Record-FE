plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.selfmedicalrecord"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.selfmedicalrecord"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Alamat backend. 10.0.2.2 = laptop yang menjalankan emulator. Untuk HP asli, ganti
        // dengan IP laptop di WiFi yang sama, mis. "http://192.168.1.8:8000/api/". Harus diakhiri "/".
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/api/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}