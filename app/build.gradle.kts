/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/search-app-circuit/blob/trunk/LICENSE
 */

plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("plugin.compose")
}

android {
  namespace = "land.sungbin.searchapp.circuit"
  compileSdk = 34

  defaultConfig {
    minSdk = 23
    targetSdk = 34
  }

  buildFeatures {
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }
}

kotlin {
  compilerOptions {
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
  }
  sourceSets.all {
    languageSettings.enableLanguageFeature("ContextReceivers")
    languageSettings.enableLanguageFeature("ExplicitBackingFields")
  }
}

composeCompiler {
  enableNonSkippingGroupOptimization = true
}

dependencies {
  implementation(libs.androidx.activity)

  implementation(libs.compose.activity)
  implementation(libs.compose.material3)

  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.immutableCollections)

  implementation(libs.timber)

  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.test.assertk)
  testImplementation(libs.test.kotlin.coroutines)
}
