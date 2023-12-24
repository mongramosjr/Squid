// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0-alpha14" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false
}

buildscript {
    repositories {
        google()
    }
    dependencies {
        val navVersion = "2.7.6"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
}