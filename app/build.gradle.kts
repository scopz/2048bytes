plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.oar.bytes"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.oar.bytes"
        minSdk = 34
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
//    implementation("androidx.preference:preference:1.1.1")
//    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation(libs.androidx.appcompat)
//    implementation("com.google.android.material:material:1.4.0")
    implementation(libs.material)
    implementation("com.google.android.flexbox:flexbox:3.0.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation(libs.androidx.constraintlayout)
//    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}