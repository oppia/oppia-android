"""
This file lists and imports all external dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

# Android SDK configuration. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
# TODO(#1542): Sync Android SDK version with the manifest.
android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
)

# Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external
RULES_JVM_EXTERNAL_TAG = "2.9"

RULES_JVM_EXTERNAL_SHA = "e5b97a31a3e8feed91636f42e19b11c49487b85e5de2f387c999ea14d77c7f45"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
RULES_KOTLIN_VERSION = "v1.5.0-alpha-2"

RULES_KOTLIN_SHA = "6194a864280e1989b6d8118a4aee03bb50edeeae4076e5bc30eef8a98dcd4f07"

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = RULES_KOTLIN_SHA,
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % RULES_KOTLIN_VERSION],
)

# TODO(#1535): Remove once rules_kotlin is released because these lines become unnecessary
load("@io_bazel_rules_kotlin//kotlin:dependencies.bzl", "kt_download_local_dev_dependencies")

kt_download_local_dev_dependencies()

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kotlin_repositories()

kt_register_toolchains()

"""
The proto_compiler and proto_java_toolchain bindings load the protos rules needed for the model
module while helping us avoid the unnecessary compilation of protoc. Referecences:
- https://github.com/google/startup-os/blob/5f30a62/WORKSPACE#L179-L187
- https://github.com/bazelbuild/bazel/issues/7095
"""
bind(
    name = "proto_compiler",
    actual = "//tools:protoc",
)

bind(
    name = "proto_java_toolchain",
    actual = "//tools:java_toolchain",
)

# The rules_java contains the java_lite_proto_library rule used in the model module.
http_archive(
    name = "rules_java",
    sha256 = "220b87d8cfabd22d1c6d8e3cdb4249abd4c93dcc152e0667db061fb1b957ee68",
    url = "https://github.com/bazelbuild/rules_java/releases/download/0.1.1/rules_java-0.1.1.tar.gz",
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

# The rules_proto contains the proto_library rule used in the model module.
http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-97d8af4dc474595af3900dd85cb3a29ad28cc313",
    urls = ["https://github.com/bazelbuild/rules_proto/archive/97d8af4dc474595af3900dd85cb3a29ad28cc313.tar.gz"],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

# Add support for Dagger
DAGGER_TAG = "2.28.1"

DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"

http_archive(
    name = "dagger",
    sha256 = DAGGER_SHA,
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

# Add support for Robolectric: https://github.com/robolectric/robolectric-bazel
http_archive(
    name = "robolectric",
    strip_prefix = "robolectric-bazel-4.x-oppia-exclusive-rc02",
    urls = ["https://github.com/oppia/robolectric-bazel/archive/4.x-oppia-exclusive-rc02.tar.gz"],
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Add support for Firebase Crashlytics
git_repository(
    name = "tools_android",
    commit = "00e6f4b7bdd75911e33c618a9bc57bab7a6e8930",
    remote = "https://github.com/bazelbuild/tools_android",
)

load("@tools_android//tools/googleservices:defs.bzl", "google_services_workspace_dependencies")

google_services_workspace_dependencies()

git_repository(
    name = "circularimageview",
    commit = "8a65ba42b3fee21b5e19ca5c8690185f7c60f65d",
    remote = "https://github.com/oppia/CircularImageview",
)

bind(
    name = "databinding_annotation_processor",
    actual = "//tools/android:compiler_annotation_processor",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = DAGGER_ARTIFACTS + [
        "android.arch.core:core-testing:1.1.1",
        "androidx.annotation:annotation:1.1.0",
        "androidx.appcompat:appcompat:1.0.2",
        "androidx.arch.core:core-testing:2.1.0",
        "androidx.constraintlayout:constraintlayout:1.1.3",
        "androidx.core:core-ktx:1.0.1",
        "androidx.core:core:1.0.1",
        "androidx.databinding:databinding-adapters:3.4.2",
        "androidx.databinding:databinding-common:3.4.2",
        "androidx.databinding:databinding-compiler:3.4.2",
        "androidx.databinding:databinding-runtime:3.4.2",
        "androidx.lifecycle:lifecycle-extensions:2.2.0",
        "androidx.lifecycle:lifecycle-livedata-core:2.2.0",
        "androidx.lifecycle:lifecycle-livedata-ktx:2.2.0",
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0",
        "androidx.multidex:multidex-instrumentation:2.0.0",
        "androidx.multidex:multidex:2.0.1",
        "androidx.navigation:navigation-fragment:2.0.0",
        "androidx.navigation:navigation-fragment-ktx:2.0.0",
        "androidx.navigation:navigation-ui:2.0.0",
        "androidx.navigation:navigation-ui-ktx:2.0.0",
        "androidx.recyclerview:recyclerview:1.0.0",
        "androidx.room:room-runtime:2.2.5",
        "androidx.test.espresso:espresso-contrib:3.1.0",
        "androidx.test.espresso:espresso-core:3.2.0",
        "androidx.test.espresso:espresso-intents:3.1.0",
        "androidx.test.ext:junit:1.1.1",
        "androidx.test:runner:1.2.0",
        "androidx.viewpager:viewpager:1.0.0",
        "androidx.work:work-runtime:2.4.0",
        "androidx.work:work-runtime-ktx:2.4.0",
        "androidx.work:work-testing:2.4.0",
        "com.android.support:support-annotations:28.0.0",
        "com.caverock:androidsvg-aar:1.4",
        "com.chaos.view:pinview:1.4.4",
        "com.crashlytics.sdk.android:crashlytics:2.9.8",
        "com.github.bumptech.glide:glide:4.11.0",
        "com.github.bumptech.glide:mocks:4.11.0",
        "com.google.android:flexbox:2.0.1",
        "com.google.android.material:material:1.2.0-alpha02",
        "com.google.firebase:firebase-analytics:17.5.0",
        "com.google.firebase:firebase-crashlytics:17.1.1",
        "com.google.gms:google-services:4.3.3",
        "com.google.guava:guava:28.1-android",
        "com.google.truth:truth:0.43",
        "com.squareup.retrofit2:converter-gson:2.5.0",
        "com.squareup.retrofit2:retrofit:2.9.0",
        "de.hdodenhof:circleimageview:3.0.1",
        "io.fabric.sdk.android:fabric:1.4.7",
        "javax.annotation:javax.annotation-api:jar:1.3.2",
        "junit:junit:4.12",
        "nl.dionsegijn:konfetti:1.2.5",
        "org.jetbrains.kotlin:kotlin-reflect:1.3.41",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk7:jar:1.3.72",
        "org.jetbrains.kotlin:kotlin-test-junit:1.3.72",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.2.2",
        "org.jetbrains:annotations:jar:13.0",
        "org.mockito:mockito-core:2.19.0",
        "org.mockito:mockito-core:2.7.22",
        "org.robolectric:annotations:4.3",
        "org.robolectric:robolectric:4.3",
    ],
    repositories = DAGGER_REPOSITORIES + [
        "https://bintray.com/bintray/jcenter",
        "https://jcenter.bintray.com/",
        "https://maven.fabric.io/public",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
