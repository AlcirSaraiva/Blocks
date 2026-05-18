plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.awesome.blocks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.awesome.blocks"
        minSdk = 24
        targetSdk = 36
        versionCode = 175
        versionName = "1.75"

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.play.services.ads)
    implementation(libs.ump)
}