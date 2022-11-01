@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  `kotlin-dsl`
  alias(libs.plugins.google.ksp)
}

repositories {
  mavenCentral()
  google()
  maven("https://plugins.gradle.org/m2/")
}

dependencies {

  implementation(platform(libs.kotlin.bom))

  compileOnly(gradleApi())

  implementation(libs.android.gradle.plugin)
  implementation(libs.dokka.gradle.plugin)
  implementation(libs.dokka.versioning)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.ktlint.core)
  implementation(libs.kotlinter)
  implementation(libs.squareup.moshi)
  implementation(libs.squareup.moshi.adapters)
  implementation(libs.vanniktech.publish)

  ksp(libs.squareup.moshi.codegen)
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
