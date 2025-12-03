// BÊN TRONG FILE build.gradle.kts (Project: StuShare)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // THÊM 2 DÒNG NÀY VÀO
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.dagger.hilt) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}