plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "asia.aaii.bagan"
    compileSdk = 35
    defaultConfig {
        applicationId = "asia.aaii.bagan"
        minSdk = 26
        targetSdk = 28
        versionCode = 390
        versionName = "3.8.8-scene01-fix2"
    }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/scene01/java"))
            res.setSrcDirs(listOf("src/scene01/res"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
