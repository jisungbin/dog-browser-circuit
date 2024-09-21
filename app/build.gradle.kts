/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.android.hilt.get().pluginId)
  id(libs.plugins.kotlin.symbolProcesing.get().pluginId)
  kotlin("android")
  kotlin("plugin.compose")
  id("kotlin-parcelize")
}

android {
  namespace = "land.sungbin.dogbrowser.circuit"
  compileSdk = 35

  defaultConfig {
    minSdk = 33
    targetSdk = 35
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  sourceSets {
    getByName("main").java.srcDir("src/main/kotlin")
  }

  testOptions.unitTests {
    isReturnDefaultValues = true
  }
}

ksp {
  arg("circuit.codegen.mode", "hilt")
}

kotlin {
  explicitApi()
  compilerOptions {
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
  }
  sourceSets.all {
    languageSettings.enableLanguageFeature("ContextReceivers")
    languageSettings.enableLanguageFeature("ExplicitBackingFields")
  }
}

composeCompiler {
  featureFlags = setOf(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.palette)

  implementation(libs.android.hilt.runtime)
  ksp(libs.android.hilt.ksp)

  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation(libs.compose.coil)
  implementation(libs.compose.telephoto)
  implementation(libs.compose.haze)
  implementation(libs.compose.activity)

  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.immutableCollections)

  implementation(libs.okio)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.coroutines)
  implementation(libs.okhttp.logging)
  implementation(libs.moshi)

  implementation(libs.circuit.foundation)
  implementation(libs.circuit.codegen.annotation)
  ksp(libs.circuit.codegen.ksp)

  implementation(libs.circuitx.android)
  implementation(libs.circuitx.overlay)

  implementation(libs.timber)

  testImplementation(kotlin("test-junit5"))
  testImplementation(kotlin("reflect")) // Used in assertk
  testImplementation(libs.test.assertk)
  testImplementation(libs.test.circuit)
  testImplementation(libs.test.turbine)
  testImplementation(libs.test.okio.fs)
  testImplementation(libs.test.kotlin.coroutines)
  testImplementation(libs.test.okhttp.mockwebserver)
}
