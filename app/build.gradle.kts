plugins {
  id("com.android.application")
  id("com.google.firebase.crashlytics")
  id("com.google.gms.google-services")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
  compileSdk = 33
  defaultConfig {
    applicationId = "org.oppia.android"
    minSdk = 21
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"
    multiDexEnabled = true
    testInstrumentationRunner = "org.oppia.android.testing.OppiaTestRunner"
    // https://developer.android.com/training/testing/junit-runner#ato-gradle
    testInstrumentationRunnerArguments["clearPackageData"] = "true"
    vectorDrawables { useSupportLibrary = true }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
//    useFir = true
    freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as? String
    kotlinCompilerVersion = rootProject.extra["kotlin_version"] as? String
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
    }
    getByName("debug") {
      isPseudoLocalesEnabled = true
    }
  }
  splits {
    // See: https://developer.android.com/studio/build/configure-apk-splits
    density {
      isEnable = false
    }
  }
  dataBinding {
    isEnabled = true
  }
  testOptions {
    // https://developer.android.com/training/testing/junit-runner#ato-gradle
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    unitTests {
      isIncludeAndroidResources = true
      all {
//        maxHeapSize = "4096m"

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
    animationsDisabled = true
  }

  // https://proandroiddev.com/isolated-fragments-unit-tests-that-run-both-instrumented-and-on-the-jvm-with-the-same-source-code-283db2e9be5d
  sourceSets {
    getByName("androidTest") {
      java.srcDirs("src/sharedTest/java")
//      kotlin.srcDirs += "src/sharedTest/java"
    }
    getByName("test") {
      java.srcDirs("src/sharedTest/java")
//      kotlin.srcDirs += "src/sharedTest/java"
      // TODO: Fix.
//      java.exclude("**/DataBinderMapperImpl.java") // Bazel-specific file used to run tests
    }
  }
  namespace = "org.oppia.android"
}

// TODO: Re-add split stuff here.

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation("androidx.appcompat:appcompat:1.0.2")
  implementation("androidx.compose.foundation:foundation:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.foundation:foundation-layout:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.material:material:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.runtime:runtime:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.runtime:runtime-livedata:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.ui:ui:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.compose.ui:ui-tooling:" + rootProject.extra["compose_version"]!! as String)
  implementation("androidx.constraintlayout:constraintlayout:1.1.3")
  implementation("androidx.core:core-ktx:1.0.2")
  implementation("androidx.legacy:legacy-support-v4:1.0.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel:2.4.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha03")
  implementation("androidx.navigation:navigation-fragment:2.0.0")
  implementation("androidx.navigation:navigation-fragment-ktx:2.0.0")
  implementation("androidx.navigation:navigation-ui:2.0.0")
  implementation("androidx.navigation:navigation-ui-ktx:2.0.0")
  implementation("androidx.fragment:fragment:" + rootProject.extra["fragment_version"]!! as String)
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha03")
  implementation("androidx.multidex:multidex:2.0.1")
  implementation("androidx.recyclerview:recyclerview:1.0.0")
  implementation("androidx.work:work-runtime-ktx:2.4.0")
  implementation("com.github.bumptech.glide:glide:4.11.0")
  implementation("com.google.android.flexbox:flexbox:3.0.0")
  implementation("com.google.android.material:material:1.3.0")
  implementation("com.google.android.material:compose-theme-adapter:" + rootProject.extra["compose_version"]!! as String)
  implementation("com.google.dagger:dagger:2.41")
  implementation("com.google.firebase:firebase-analytics:17.5.0")
  implementation("com.google.firebase:firebase-analytics-ktx:17.5.0")
  implementation("com.google.firebase:firebase-appcheck:16.0.0")
  implementation("com.google.firebase:firebase-appcheck-debug:16.0.0")
  implementation("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
  implementation("com.google.firebase:firebase-core:17.5.0")
  implementation("com.google.firebase:firebase-crashlytics:17.0.0")
  implementation("com.google.firebase:firebase-firestore-ktx:24.2.1")
  implementation("com.google.firebase:firebase-auth-ktx:19.3.1")
  implementation("com.google.guava:guava:28.1-android")
  implementation("com.google.protobuf:protobuf-javalite:3.17.3")
  implementation("nl.dionsegijn:konfetti:1.2.5")
  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
  implementation("org.mockito:mockito-core:2.7.22")
  implementation("com.github.oppia:android-spotlight:cc23499d37dc8533a2876e45b5063e981a4583f4")
  compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
  compileOnly("javax.annotation:javax.annotation-api:1.3.2")
  compileOnly("org.glassfish.jaxb:jaxb-runtime:2.3.2")
  testImplementation("androidx.compose.ui:ui-test-junit4:" + rootProject.extra["compose_version"]!! as String)
  testImplementation("androidx.test:core:1.2.0")
  testImplementation("androidx.test.espresso:espresso-contrib:3.1.0")
  testImplementation("androidx.test.espresso:espresso-core:3.2.0")
  testImplementation("androidx.test.espresso:espresso-intents:3.1.0")
  testImplementation("androidx.test.ext:junit:1.1.1")
  testImplementation("androidx.test.ext:truth:1.4.0")
  testImplementation("androidx.work:work-testing:2.4.0")
  testImplementation("com.github.bumptech.glide:mocks:4.11.0")
  testImplementation("com.google.truth:truth:1.1.3")
  testImplementation("com.google.truth.extensions:truth-liteproto-extension:1.1.3")
  testImplementation("org.robolectric:annotations:4.5")
  testImplementation("org.robolectric:robolectric:4.5")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  testImplementation(kotlin("test-junit", version = rootProject.extra["kotlin_version"] as? String))
  testImplementation("org.mockito:mockito-core:2.7.22")
  testImplementation(project(":testing"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4:" + rootProject.extra["compose_version"]!! as String)
  androidTestImplementation("androidx.test:core:1.2.0")
  androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
  androidTestImplementation("androidx.test.espresso:espresso-intents:3.1.0")
  androidTestImplementation("androidx.test.ext:junit:1.1.1")
  androidTestImplementation("androidx.test.ext:truth:1.4.0")
  androidTestImplementation("com.github.bumptech.glide:mocks:4.11.0")
  androidTestImplementation("com.google.truth:truth:1.1.3")
  androidTestImplementation("androidx.work:work-testing:2.4.0")
  androidTestImplementation("com.google.truth.extensions:truth-liteproto-extension:1.1.3")
  androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  androidTestImplementation("org.mockito:mockito-android:2.7.22")
  androidTestImplementation("org.robolectric:annotations:4.5")
  // Adding the testing module directly causes duplicates of the below groups so we need to
  // exclude them before adding the testing module to the androidTestImplementation(dependencies)
  // TODO: Fix.
//  androidTestImplementation(project(":testing")) {
//    exclude(group = "org.apache.maven",) module = "maven-artifact"
//    exclude(group = "org.apache.maven",) module = "maven-artifact-manager"
//    exclude(group = "org.apache.maven",) module = "maven-model"
//    exclude(group = "org.apache.maven",) module = "maven-plugin-registry"
//    exclude(group = "org.apache.maven",) module = "maven-profile"
//    exclude(group = "org.apache.maven",) module = "maven-project"
//    exclude(group = "org.apache.maven",) module = "maven-settings"
//    exclude(group = "org.apache.maven",) module = "maven-error-diagnostics"
//    exclude(group = "org.apache.maven",) module = "maven-ant-tasks"
//    exclude(group = "org.apache.maven.wagon")
//    exclude(group = "org.codehaus.plexus")
//    exclude(group = "org.robolectric")
//  }
  androidTestUtil("androidx.test:orchestrator:1.2.0")
  kapt("com.google.dagger:dagger-compiler:2.41")
  kaptTest("com.google.dagger:dagger-compiler:2.41")
  kaptAndroidTest("com.google.dagger:dagger-compiler:2.41")
  api(project(":data"))
  implementation(project(":model"))
  testImplementation(project(":model"))
  androidTestImplementation(project(":model"))
  implementation(project(":domain"))
  implementation(project(":utility"))
}

// TODO: Re-add helpers.
