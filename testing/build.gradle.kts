plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
  compileSdk = 33

  defaultConfig {
    minSdk = 21
    targetSdk = 33
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        // Enable forking to ensure each test is properly run in isolation. For context, see:
        // https://discuss.gradle.org/t/36066/2 & https://github.com/oppia/oppia-android/issues/1942
        it.setForkEvery(1)
        it.maxParallelForks = Runtime.getRuntime().availableProcessors()

        // https://discuss.gradle.org/t/29495/2 & https://stackoverflow.com/a/34299238.
        it.testLogging {
          events("passed", "skipped", "failed")
          showExceptions = true
          exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
          showCauses = true
          showStackTraces = true
          showStandardStreams = false
        }
      }
    }
  }
}

// See notice for the excluded files in domain/build.gradle for an explanation. Note that these
// tests can't be built in Gradle since they depend on app layer dependencies & Gradle doesn't allow
// a dependency between this & the app module since the latter is an Android application rather than
// a library.
val filesToExclude = listOf(
  "**/*InitializeDefaultLocaleRuleCustomContextTest*.kt",
  "**/*InitializeDefaultLocaleRuleOmissionTest*.kt",
  "**/*InitializeDefaultLocaleRuleTest*.kt",
  "**/*TestSyncStatusManagerTest*.kt" // This depends on another module's test base.
)
tasks.withType(SourceTask::class).configureEach { exclude(filesToExclude) }
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
  exclude(filesToExclude)
}
android.sourceSets.getByName("test").java.exclude(filesToExclude)

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation("androidx.annotation:annotation:1.1.0")
  implementation("androidx.appcompat:appcompat:1.1.0")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03")
  implementation("androidx.core:core-ktx:1.0.2")
  implementation("androidx.test:core:1.0.0")
  implementation("androidx.test.espresso:espresso-accessibility:3.1.0")
  implementation("androidx.test.espresso:espresso-core:3.2.0")
  implementation("androidx.test.ext:truth:1.4.0")
  implementation("androidx.test:runner:1.2.0")
  implementation("com.google.android.material:material:1.3.0")
  implementation("com.google.dagger:dagger:2.41")
  implementation("com.google.firebase:firebase-auth-ktx:19.3.1")
  implementation("com.google.protobuf:protobuf-javalite:3.17.3")
  implementation("com.google.truth:truth:1.1.3")
  implementation("com.google.truth.extensions:truth-liteproto-extension:1.1.3")
  implementation("nl.dionsegijn:konfetti:1.2.5")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  implementation("org.robolectric:robolectric:4.5")
  implementation(kotlin("reflect", version = rootProject.extra["kotlin_version"] as? String))
  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.4")
  implementation("org.mockito:mockito-core:2.19.0")
  implementation(project(":domain"))
  implementation(project(":model"))
  implementation(project(":utility"))
  compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
  compileOnly("javax.annotation:javax.annotation-api:1.3.2")
  compileOnly("org.glassfish.jaxb:jaxb-runtime:2.3.2")
  testImplementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03")
  testImplementation("androidx.test.ext:junit:1.1.1")
  testImplementation("com.google.android.material:material:1.3.0")
  testImplementation("com.google.truth:truth:1.1.3")
  testImplementation("junit:junit:4.12")
  testImplementation(kotlin("reflect", version = rootProject.extra["kotlin_version"] as? String))
  testImplementation("org.mockito:mockito-core:2.19.0")
  testImplementation(project(":domain"))
  kapt("com.google.dagger:dagger-compiler:2.41")
  kaptTest("com.google.dagger:dagger-compiler:2.41")
  annotationProcessor("com.google.auto.service:auto-service:1.0-rc4")
  implementation(project(":model"))
}
