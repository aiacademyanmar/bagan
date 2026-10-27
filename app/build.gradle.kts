plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "asia.aaii.baganmuseum"
    compileSdk = 36
    defaultConfig {
        applicationId = "asia.aaii.baganmuseum"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.6.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.ar:core:1.56.0")
    implementation("com.google.zxing:core:3.5.3")
}
