# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
This package contains all third-party dependencies that the project depends on. Each maven artifact
should correspond to a single wrapper android_library, and versions should go in versions.bzl.

Note to developers: most new dependencies should go in versions.bzl, not here. In some circumstances
(such as when non-Maven dependencies need to be used) it's appropriate to add additional
dependencies here.

Further, note that not all dependencies can be encapsulated here. For example, Dagger introduces its
own Bazel macros to automatically set up code generation (which includes pulling in external Dagger
dependencies).
"""

load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")
load(":versions.bzl", "MAVEN_PRODUCTION_DEPENDENCY_VERSIONS", "MAVEN_TEST_DEPENDENCY_VERSIONS")

# Create an android library wrapper for all production dependencies. Note that artifact is used so
# that the correct Maven repository (default) is selected. This allows future customization
# potential in managing multiple Maven repositories.
[alias(
    name = name.replace(":", "_").replace(".", "_"),
    actual = artifact("%s:%s" % (name, version)),
    visibility = ["//visibility:public"],
) for name, version in MAVEN_PRODUCTION_DEPENDENCY_VERSIONS.items()]

# Create an android library wrapper for all test dependencies.
[alias(
    name = name.replace(":", "_").replace(".", "_"),
    testonly = True,
    actual = artifact("%s:%s" % (name, version)),
    visibility = ["//visibility:public"],
) for name, version in MAVEN_TEST_DEPENDENCY_VERSIONS.items()]

# A wrapper dependency for the custom Git import of Android SVG (so that dependencies on this
# library still routes through //third_party).
alias(
    name = "com_caverock_androidsvg",
    actual = "@androidsvg//androidsvg",
    visibility = ["//visibility:public"],
)

alias(
    name = "com_github_takusemba_spotlight",
    actual = "@android-spotlight//spotlight",
    visibility = ["//visibility:public"],
)

alias(
    name = "robolectric_android-all",
    testonly = True,
    actual = "@robolectric//bazel:android-all",
    visibility = ["//visibility:public"],
)

alias(
    name = "com_google_archivepatcher",
    actual = "@archive_patcher//tools",
    visibility = ["//visibility:public"],
)

java_library(
    name = "moshi",
    exported_plugins = ["//tools:moshi_annotation_processor_plugin"],
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:com_squareup_moshi_moshi-kotlin",
    ],
)

alias(
    name = "io_github_karino2_kotlitex",
    actual = "@kotlitex//kotlitex",
    visibility = ["//visibility:public"],
)

alias(
    name = "oppia_proto_api_protos",
    actual = "@oppia_proto_api//:android_protos",
    visibility = ["//scripts:oppia_script_library_visibility"],
)

alias(
    name = "oppia_proto_api_java_protos",
    testonly = True,
    actual = "@oppia_proto_api//:android_java_protos",
    visibility = ["//scripts:oppia_script_library_visibility"],
)

# Creates an alias for Guava Android to support maven_install's override of Guava. This ensures that
# Guava-Android is always used in builds, even if dependencies request a newer version of Guava-JRE.
android_library(
    name = "com_google_guava_guava",
    visibility = ["//visibility:public"],
    exports = [
        "//third_party:com_google_errorprone_error_prone_annotations",
        "//third_party:com_google_guava_failureaccess",
        "//third_party:com_google_j2objc_j2objc-annotations",
        "//third_party:org_checkerframework_checker-compat-qual",
        "//third_party:org_checkerframework_checker-qual",
        "@guava_android//jar",
    ],
)

java_library(
    name = "com_google_guava_guava_jre",
    visibility = ["//scripts:__subpackages__"],
    exports = [
        "//third_party:com_google_errorprone_error_prone_annotations",
        "//third_party:com_google_guava_failureaccess",
        "//third_party:com_google_j2objc_j2objc-annotations",
        "//third_party:org_checkerframework_checker-compat-qual",
        "//third_party:org_checkerframework_checker-qual",
        "@guava_jre//jar",
    ],
)

# Define a separate target for the Glide annotation processor compiler. Unfortunately, this library
# can't encapsulate all of Glide (i.e. by exporting the main Glide dependency) since that includes
# Android assets which java_library targets do not export.
java_library(
    name = "glide_compiler",
    exported_plugins = ["//tools:glide_compiler_plugin"],
    visibility = ["//visibility:public"],
)

alias(
    name = "android_bundletool",
    actual = "@android_bundletool//jar",
    visibility = ["//visibility:public"],
)

java_binary(
    name = "android_bundletool_binary",
    main_class = "com.android.tools.build.bundletool.BundleToolMain",
    visibility = ["//visibility:public"],
    runtime_deps = [":android_bundletool"],
)
