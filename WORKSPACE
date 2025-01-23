"""
This file lists and imports all external dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load("//:build_vars.bzl", "BUILD_SDK_VERSION", "BUILD_TOOLS_VERSION")
load("//third_party:versions.bzl", "HTTP_DEPENDENCY_VERSIONS", "MAVEN_REPOSITORIES", "get_maven_dependencies")

# Android SDK configuration. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
# TODO(#1542): Sync Android SDK version with the manifest.
android_sdk_repository(
    name = "androidsdk",
    api_level = BUILD_SDK_VERSION,
    build_tools_version = BUILD_TOOLS_VERSION,
)

# The rules_java contains the java_lite_proto_library rule used in the model module.
http_archive(
    name = "rules_java",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_java"]["sha"],
    url = "https://github.com/bazelbuild/rules_java/releases/download/{0}/rules_java-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_java"]["version"]),
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

# Oppia's backend proto API definitions.
git_repository(
    name = "oppia_proto_api",
    commit = HTTP_DEPENDENCY_VERSIONS["oppia_proto_api"]["version"],
    remote = "https://github.com/oppia/oppia-proto-api",
    shallow_since = "1716846301 -0700",
)

load("@oppia_proto_api//repo:deps.bzl", "initializeDepsForWorkspace")

initializeDepsForWorkspace()

load("@oppia_proto_api//repo:toolchains.bzl", "initializeToolchainsForWorkspace")

initializeToolchainsForWorkspace()

# Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external
http_archive(
    name = "rules_jvm_external",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["sha"],
    strip_prefix = "rules_jvm_external-%s" % HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["version"],
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % HTTP_DEPENDENCY_VERSIONS["rules_jvm"]["version"],
)

# Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
http_archive(
    name = "io_bazel_rules_kotlin",
    patches = ["//tools/kotlin:remove_processor_duplicates.patch"],
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["sha"],
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["version"]],
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")

# Use the 1.6 compiler since Kotlin 1.6 is the current supported version in the repository.
kotlin_repositories(
    compiler_release = kotlinc_version(
        release = "1.6.10",
        sha256 = "432267996d0d6b4b17ca8de0f878e44d4a099b7e9f1587a98edc4d27e76c215a",
    ),
)

register_toolchains("//tools/kotlin:kotlin_16_jdk9_toolchain")

# The proto_compiler and proto_java_toolchain bindings load the protos rules needed for the model
# module while helping us avoid the unnecessary compilation of protoc. Referecences:
# - https://github.com/google/startup-os/blob/5f30a62/WORKSPACE#L179-L187
# - https://github.com/bazelbuild/bazel/issues/7095

bind(
    name = "proto_compiler",
    actual = "//tools:protoc",
)

bind(
    name = "proto_java_toolchain",
    actual = "//tools:java_toolchain",
)

# The rules_proto contains the proto_library rule used in the model module.
http_archive(
    name = "rules_proto",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_proto"]["sha"],
    strip_prefix = "rules_proto-%s" % HTTP_DEPENDENCY_VERSIONS["rules_proto"]["version"],
    urls = ["https://github.com/bazelbuild/rules_proto/archive/%s.tar.gz" % HTTP_DEPENDENCY_VERSIONS["rules_proto"]["version"]],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

# Add support for Dagger
http_archive(
    name = "dagger",
    sha256 = HTTP_DEPENDENCY_VERSIONS["dagger"]["sha"],
    strip_prefix = "dagger-dagger-%s" % HTTP_DEPENDENCY_VERSIONS["dagger"]["version"],
    urls = ["https://github.com/google/dagger/archive/dagger-%s.zip" % HTTP_DEPENDENCY_VERSIONS["dagger"]["version"]],
)

load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")

# Add support for Robolectric: https://github.com/robolectric/robolectric-bazel
http_archive(
    name = "robolectric",
    sha256 = HTTP_DEPENDENCY_VERSIONS["robolectric"]["sha"],
    strip_prefix = "robolectric-bazel-%s" % HTTP_DEPENDENCY_VERSIONS["robolectric"]["version"],
    urls = ["https://github.com/robolectric/robolectric-bazel/archive/%s.tar.gz" % HTTP_DEPENDENCY_VERSIONS["robolectric"]["version"]],
)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Add support for Firebase Crashlytics
git_repository(
    name = "tools_android",
    commit = "00e6f4b7bdd75911e33c618a9bc57bab7a6e8930",
    remote = "https://github.com/bazelbuild/tools_android",
    shallow_since = "1594238320 -0400",
)

load("@tools_android//tools/googleservices:defs.bzl", "google_services_workspace_dependencies")

google_services_workspace_dependencies()

# A custom version of Android SVG is needed since custom changes needed to be added to the library
# to correctly size in-line SVGs (such as those needed for LaTeX-based math expressions).
git_repository(
    name = "androidsvg",
    commit = "5bc9c7553e94c3476e8ea32baea3c77567228fcd",
    remote = "https://github.com/oppia/androidsvg",
    shallow_since = "1686304726 -0700",
)

git_repository(
    name = "android-spotlight",
    commit = "cc23499d37dc8533a2876e45b5063e981a4583f4",
    remote = "https://github.com/oppia/android-spotlight",
    shallow_since = "1680147372 -0700",
)

# A custom fork of KotliTeX that removes resources artifacts that break the build, and updates the
# min target SDK version to be compatible with Oppia.
git_repository(
    name = "kotlitex",
    commit = "ccdf4170817fa3b48b8e1e452772dd58ecb71cf2",
    remote = "https://github.com/oppia/kotlitex",
    shallow_since = "1679426649 -0700",
)

git_repository(
    name = "archive_patcher",
    commit = "d1c18b0035d5f669ddaefadade49cae0748f9df2",
    remote = "https://github.com/oppia/archive-patcher",
    shallow_since = "1642022460 -0800",
)

bind(
    name = "databinding_annotation_processor",
    actual = "//tools/android:compiler_annotation_processor",
)

http_archive(
    name = "protobuf_tools",
    sha256 = HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["sha"],
    strip_prefix = "protobuf-%s" % HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["version"],
    urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v{0}/protobuf-all-{0}.zip".format(HTTP_DEPENDENCY_VERSIONS["protobuf_tools"]["version"])],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

ATS_TAG = "1edfdab3134a7f01b37afabd3eebfd2c5bb05151"

ATS_SHA256 = "dcd1ff76aef1a26329d77863972780c8fe1fc8ff625747342239f0489c2837ec"

http_archive(
    name = "android_test_support",
    sha256 = ATS_SHA256,
    strip_prefix = "android-test-%s" % ATS_TAG,
    urls = ["https://github.com/android/android-test/archive/%s.tar.gz" % ATS_TAG],
)

load("@android_test_support//:repo.bzl", "android_test_repositories")

android_test_repositories()

# Android bundle tool.
http_jar(
    name = "android_bundletool",
    sha256 = HTTP_DEPENDENCY_VERSIONS["android_bundletool"]["sha"],
    url = "https://github.com/google/bundletool/releases/download/{0}/bundletool-all-{0}.jar".format(HTTP_DEPENDENCY_VERSIONS["android_bundletool"]["version"]),
)

# Note to developers: new dependencies should be added to //third_party:versions.bzl, not here.
maven_install(
    artifacts = DAGGER_ARTIFACTS + get_maven_dependencies(),
    duplicate_version_warning = "error",
    fail_if_repin_required = True,
    maven_install_json = "//third_party:maven_install.json",
    override_targets = {
        "com.google.guava:guava": "@//third_party:com_google_guava_guava",
    },
    repositories = DAGGER_REPOSITORIES + MAVEN_REPOSITORIES,
    strict_visibility = True,
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

[
    http_jar(
        name = "guava_%s" % guava_type,
        sha256 = HTTP_DEPENDENCY_VERSIONS["guava_%s" % guava_type]["sha"],
        urls = [
            "{0}/com/google/guava/guava/{1}-{2}/guava-{1}-{2}.jar".format(
                url_base,
                HTTP_DEPENDENCY_VERSIONS["guava_%s" % guava_type]["version"],
                guava_type,
            )
            for url_base in DAGGER_REPOSITORIES + MAVEN_REPOSITORIES
        ],
    )
    for guava_type in [
        "android",
        "jre",
    ]
]
