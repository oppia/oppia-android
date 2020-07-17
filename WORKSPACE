load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
    #build_tools_version = "28.0.2",
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

# rules_proto defines abstract rules for building Protocol Buffers.
http_archive(
    name = "rules_proto",
    sha256 = "2490dca4f249b8a9a3ab07bd1ba6eca085aaf8e45a734af92aad0c42d9dc7aaf",
    strip_prefix = "rules_proto-218ffa7dfa5408492dc86c01ee637614f8695c45",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
        "https://github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
    ],
)

#Add support for Dagger
DAGGER_TAG = "2.28.1"
DAGGER_SHA = "9e69ab2f9a47e0f74e71fe49098bea908c528aa02fa0c5995334447b310d0cdd"
http_archive(
    name = "dagger",
    strip_prefix = "dagger-dagger-%s" % DAGGER_TAG,
    sha256 = DAGGER_SHA,
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % DAGGER_TAG],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

#Add support for Robolectric: http://robolectric.org/getting-started/
http_archive(
 name = "robolectric",
 urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.0.1.tar.gz"],
 strip_prefix = "robolectric-bazel-4.0.1",
 sha256 = "dff7a1f8e7bd8dc737f20b6bbfaf78d8b5851debe6a074757f75041029f0c43b",
)
load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")
robolectric_repositories()

maven_install(
    artifacts = DAGGER_ARTIFACTS,
    repositories = DAGGER_REPOSITORIES,
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

