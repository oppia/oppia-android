plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
  namespace = "org.oppia.android.domain"
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
    freeCompilerArgs += listOf("-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi")
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

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
    }
  }
}

// These tests aren't supported in Gradle since they make use of test resources & AGP doesn't
// support merging resources for test builds, or they rely on a compile-time built proto
// configuration file which the current Gradle configuration doesn't support. This test runs
// correctly for Bazel & is included in the CI workflow that runs Bazel tests.
// https://stackoverflow.com/a/69141612 seems like the only solution that actually works (versus
// trying to exclude via sourceSets), so the following is an adapted version that ensures all
// generated sources that may reference the test also don"t exist (such as Dagger running to
// generate a test application component). Note that this must exist in tandem with the sourceSet
// exclusion in order to properly work.
val filesToExclude = listOf(
  "**/*LanguageConfigRetrieverTest*.kt",
  "**/*LanguageConfigRetrieverProductionTest*.kt",
  "**/*LocaleControllerTest*.kt",
  "**/*TranslationControllerTest*.kt"
)
tasks.withType(SourceTask::class).configureEach { exclude(filesToExclude) }
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
  exclude(filesToExclude)
}
android.sourceSets.getByName("test").java.exclude(filesToExclude)

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation("androidx.appcompat:appcompat:1.0.2")
  implementation("androidx.exifinterface:exifinterface:1.0.0-rc01")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03")
  implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
  implementation("androidx.work:work-runtime-ktx:2.4.0")
  implementation("com.google.dagger:dagger:2.41")
  implementation("com.google.firebase:firebase-analytics-ktx:17.5.0")
  implementation("com.google.firebase:firebase-crashlytics:17.0.0")
  implementation("com.google.firebase:firebase-firestore-ktx:24.2.1")
  implementation("com.google.firebase:firebase-auth-ktx:19.3.1")
  implementation("com.google.guava:guava:28.1-android")
  implementation("com.google.protobuf:protobuf-javalite:3.17.3")
  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.4")
  compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
  compileOnly("javax.annotation:javax.annotation-api:1.3.2")
  compileOnly("org.glassfish.jaxb:jaxb-runtime:2.3.2")
  testImplementation("androidx.test.espresso:espresso-core:3.2.0")
  testImplementation("androidx.test.ext:junit:1.1.1")
  testImplementation("androidx.test.ext:truth:1.4.0")
  testImplementation("androidx.work:work-testing:2.4.0")
  testImplementation("com.google.dagger:dagger:2.41")
  testImplementation("com.google.truth.extensions:truth-liteproto-extension:1.1.3")
  testImplementation("com.google.truth:truth:1.1.3")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.7.2")
  testImplementation("junit:junit:4.12")
  testImplementation(kotlin("test-junit", version = rootProject.extra["kotlin_version"] as? String))
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  testImplementation("org.mockito:mockito-core:3.9.0")
  testImplementation("org.robolectric:robolectric:4.5")
  testImplementation(project(":testing"))
  kapt("com.google.dagger:dagger-compiler:2.41")
  kaptTest("com.google.dagger:dagger-compiler:2.41")
  api(project(":data"))
  implementation(project(":model"))
  implementation(project(":utility"))
}
