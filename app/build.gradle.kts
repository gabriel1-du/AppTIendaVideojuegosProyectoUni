import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.videojuegosandroidtienda"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.videojuegosandroidtienda"
        minSdk = 24
        targetSdk = 36
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Image loading
    implementation(libs.coil)
    // JWT Decode
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation(libs.firebase.appdistribution.gradle)
    // Network inspector
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chuckerNoOp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation("com.squareup.okio:okio:3.9.0")
}