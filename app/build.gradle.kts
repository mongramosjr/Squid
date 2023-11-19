plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.squidsentry.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.squidsentry.mobile"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // For Jetpack Compose.
    implementation("com.patrykandpatrick.vico:compose:1.12.0")
    // For `compose`. Creates a `ChartStyle` based on an M2 Material Theme.
    implementation("com.patrykandpatrick.vico:compose-m2:1.12.0")
    // For `compose`. Creates a `ChartStyle` based on an M3 Material Theme.
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
    // Houses the core logic for charts and other elements. Included in all other modules.
    implementation("com.patrykandpatrick.vico:core:1.12.0")
    // For the view system.
    implementation("com.patrykandpatrick.vico:views:1.12.0")

    //implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
}