# Oppia Android's root WORKSPACE file.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Infrastructure versions.
RULES_KOTLIN_VERSION = "9051eb053f9c958440603d557316a6e9fda14687"
RULES_PROTO_VERSION = "97d8af4dc474595af3900dd85cb3a29ad28cc313"
RULES_JVM_EXTERNAL_VERSION = "2.10"

# Top level Android configuration.
android_sdk_repository(name = "androidsdk")

# Kotlin support in Bazel.
http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = "c36e71eec84c0e17dd098143a9d93d5720e81b4db32bceaf2daf939252352727",
    strip_prefix = "rules_kotlin-%s" % RULES_KOTLIN_VERSION,
    url = "https://github.com/bazelbuild/rules_kotlin/archive/%s.tar.gz" % RULES_KOTLIN_VERSION,
)
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

# Proto support in Bazel.
http_archive(
    name = "rules_proto",
    sha256 = "602e7161d9195e50246177e7c55b2f39950a9cf7366f74ed5f22fd45750cd208",
    strip_prefix = "rules_proto-%s" % RULES_PROTO_VERSION,
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_VERSION,
        "https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % RULES_PROTO_VERSION,
    ],
)
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

# Maven setup.
# TODO(BenHenning): Make remote download work.
local_repository(
    name = "rules_jvm_external",
    path = "C:/Users/Ben/Desktop/rules_jvm_external",
)
load("@rules_jvm_external//:defs.bzl", "maven_install")
#http_archive(
#    name = "rules_jvm_external",
#    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_VERSION,
#    sha256 = "1bbf2e48d07686707dd85357e9a94da775e1dbd7c464272b3664283c9c716d26",
#    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_VERSION,
#)
#load("@rules_jvm_external//:defs.bzl", "maven_install")
#maven_install(
#    artifacts = [
#      "androidx.appcompat:appcompat:1.0.2",
#      "androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03",
#      "com.github.bumptech.glide:glide:4.9.0",
#      "com.google.dagger:dagger:2.24",
#      "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41",
#    ],
#    repositories = [
#        "https://maven.google.com",
#        "https://repo1.maven.org/maven2",
#    ],
#)

# Square's Maven setup.
MAVEN_REPOSITORY_RULES_VERSION = "1.0"
http_archive(
    name = "maven_repository_rules",
    urls = ["https://github.com/square/bazel_maven_repository/archive/%s.zip" % MAVEN_REPOSITORY_RULES_VERSION],
    type = "zip",
    strip_prefix = "bazel_maven_repository-%s" % MAVEN_REPOSITORY_RULES_VERSION,
    sha256 = "5950eb0e4a3b8fd39832e58dd30e96258526751dabdc308cc7216f74396d8d41",
)
load("@maven_repository_rules//maven:maven.bzl", "maven_repository_specification")
maven_repository_specification(
    name = "maven",
    artifacts = {
       #"androidx.appcompat:appcompat:1.0.2": { "sha256": "3fd4341776428c7e0e5c18a7c10de129475b69ab9d30aeafbb5c277bb6074fa9" },
       #"androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03": { "sha256": "3fd4341776428c7e0e5c18a7c10de129475b69ab9d30aeafbb5c277bb6074fa9" },
       #"com.github.bumptech.glide:glide:4.9.0": { "sha256": "3fd4341776428c7e0e5c18a7c10de129475b69ab9d30aeafbb5c277bb6074fa9" },
       "com.google.dagger:dagger:2.24": { "insecure": True },
       #"org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.41": { "sha256": "3fd4341776428c7e0e5c18a7c10de129475b69ab9d30aeafbb5c277bb6074fa9" },
       "javax.inject:javax.inject:1": { "insecure": True },
    },
)
