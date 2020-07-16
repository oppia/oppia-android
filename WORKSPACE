load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
    build_tools_version = "28.0.2",
)

#Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external
RULES_JVM_EXTERNAL_TAG = "2.9"
RULES_JVM_EXTERNAL_SHA = "e5b97a31a3e8feed91636f42e19b11c49487b85e5de2f387c999ea14d77c7f45"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
RULES_KOTLIN_VERSION = "legacy-1.3.0-rc4"
RULES_KOTLIN_SHA = "fe32ced5273bcc2f9e41cea65a28a9184a77f3bc30fea8a5c47b3d3bfc801dff"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % RULES_KOTLIN_VERSION],
    type = "zip",
    strip_prefix = "rules_kotlin-%s" % RULES_KOTLIN_VERSION,
    sha256 = RULES_KOTLIN_SHA,
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

#Add support for Dagger: https://github.com/google/dagger
DAGGER_TAG = "2.28.1"
DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"
http_archive(
    name = "dagger",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    sha256 = DAGGER_SHA,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

#Add support for Retrofit
jvm_maven_import_external(
    name = "retrofit",
    artifact = "com.squareup.retrofit2:retrofit:2.9.0",
    server_urls = ["http://central.maven.org/maven2"],
    licenses = ["notice"],  # Apache 2.0
)

#Add support for Data-binding
bind(
  name = "databinding_annotation_processor",
  actual = "//tools/android:compiler_annotation_processor",
)

maven_install(
    artifacts = DAGGER_ARTIFACTS + [
      "com.android.databinding:adapters:3.4.2",
      "com.android.databinding:library:3.4.2",
      "com.android.databinding:baseLibrary:3.4.2",
      "com.android.support:support-annotations:28.0.0",
      "com.crashlytics.sdk.android:crashlytics:2.9.8",
      "io.fabric.sdk.android:fabric:1.4.7",
      "com.squareup.retrofit2:retrofit:2.9.0",
      "com.squareup.retrofit2:converter-gson:2.5.0",
      "com.google.gms:google-services:4.3.3",
    ],
    repositories = DAGGER_REPOSITORIES + [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://maven.fabric.io/public",
        "https://bintray.com/bintray/jcenter",
    ],
)

#TODO: Add tools_android with the following change:
#https://github.com/bazelbuild/tools_android/commit/58d67fd54a3b7f5f1e6ddfa865442db23a60e1b6