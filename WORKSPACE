"""
This file lists and imports all external dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//third_party:versions.bzl", "HTTP_DEPENDENCY_VERSIONS", "get_maven_dependencies")

# Android SDK configuration. For more details, see:
# https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository
# TODO(#1542): Sync Android SDK version with the manifest.
android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
)

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
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["sha"],
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % HTTP_DEPENDENCY_VERSIONS["rules_kotlin"]["version"]],
)

# TODO(#1535): Remove once rules_kotlin is released because these lines become unnecessary
load("@io_bazel_rules_kotlin//kotlin:dependencies.bzl", "kt_download_local_dev_dependencies")

kt_download_local_dev_dependencies()

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kotlin_repositories()

kt_register_toolchains()

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

# The rules_java contains the java_lite_proto_library rule used in the model module.
http_archive(
    name = "rules_java",
    sha256 = HTTP_DEPENDENCY_VERSIONS["rules_java"]["sha"],
    url = "https://github.com/bazelbuild/rules_java/releases/download/{0}/rules_java-{0}.tar.gz".format(HTTP_DEPENDENCY_VERSIONS["rules_java"]["version"]),
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

rules_java_dependencies()

rules_java_toolchains()

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

# Note to developers: new dependencies should be added to //third_party:versions.bzl, not here.
maven_install(
    artifacts = DAGGER_ARTIFACTS + get_maven_dependencies(),
    repositories = DAGGER_REPOSITORIES + [
        "https://bintray.com/bintray/jcenter",
        "https://jcenter.bintray.com/",
        "https://maven.fabric.io/public",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)
