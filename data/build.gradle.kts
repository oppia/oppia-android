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
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    multiDexEnabled = true
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

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
    }
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation("androidx.appcompat:appcompat:1.0.2")
  implementation("com.google.dagger:dagger:2.41")
  implementation("com.google.guava:guava:28.1-android")
  implementation("com.google.protobuf:protobuf-javalite:3.17.3")
  implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
  implementation("com.squareup.okhttp3:okhttp:4.7.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
  compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
  compileOnly("javax.annotation:javax.annotation-api:1.3.2")
  compileOnly("org.glassfish.jaxb:jaxb-runtime:2.3.2")
  testImplementation("androidx.test.ext:junit:1.1.1")
  testImplementation("androidx.test.ext:truth:1.4.0")
  testImplementation("com.google.dagger:dagger:2.41")
  testImplementation("com.google.truth:truth:1.1.3")
  testImplementation("com.google.truth.extensions:truth-liteproto-extension:1.1.3")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.7.2")
  testImplementation("com.squareup.okhttp3:okhttp:4.7.2")
  testImplementation("junit:junit:4.12")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  testImplementation("org.mockito:mockito-core:2.19.0")
  testImplementation("org.robolectric:robolectric:4.5")
  testImplementation(project(":testing"))
  // TODO(#97): Isolate retrofit-mock dependency from production
  api("com.squareup.retrofit2:converter-moshi:2.7.2")
  api("com.squareup.retrofit2:retrofit:2.7.2")
  api("com.squareup.retrofit2:retrofit-mock:2.7.2")
  androidTestImplementation("androidx.test:runner:1.2.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
  kapt("com.google.dagger:dagger-compiler:2.41")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")
  kaptTest("com.google.dagger:dagger-compiler:2.41")
  implementation(project(":utility"))
  implementation(project(":model"))
  testImplementation(project(":model"))
}
