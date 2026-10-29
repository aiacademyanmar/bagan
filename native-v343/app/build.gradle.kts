plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}
android {
  namespace = "asia.aaii.bagan"
  compileSdk = 36
  defaultConfig {
    applicationId = "asia.aaii.bagan.museum"
    minSdk = 26
    targetSdk = 35
    versionCode = 375
    versionName = "3.7.5"
  }
  buildTypes { getByName("release") { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
  buildFeatures { compose = true; buildConfig = true }
}
dependencies {
  implementation(platform("androidx.compose:compose-bom:2026.05.00"))
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  testImplementation("junit:junit:4.13.2")
  implementation("io.github.sceneview:sceneview:4.34.0")
  implementation("io.github.sceneview:arsceneview:4.34.0")
}
