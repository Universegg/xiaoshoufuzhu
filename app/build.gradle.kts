plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.xiaoshoufuzhu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.xiaoshoufuzhu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro")
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
    implementation(files("libs/mysql-connector-java-5.1.49-bin.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.belerweb:pinyin4j:2.5.1")
}