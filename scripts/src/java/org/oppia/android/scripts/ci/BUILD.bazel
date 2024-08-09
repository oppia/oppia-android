"""
Libraries corresponding to developer scripts that help with continuous integration workflows.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "compute_affected_tests_lib",
    testonly = True,
    srcs = [
        "ComputeAffectedTests.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/common:git_client",
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//scripts/src/java/org/oppia/android/scripts/proto:affected_tests_java_proto",
    ],
)

kt_jvm_library(
    name = "retrieve_affected_tests_lib",
    testonly = True,
    srcs = [
        "RetrieveAffectedTests.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//scripts/src/java/org/oppia/android/scripts/proto:affected_tests_java_proto",
    ],
)

kt_jvm_library(
    name = "compute_changed_files_lib",
    testonly = True,
    srcs = [
        "ComputeChangedFiles.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:git_client",
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//scripts/src/java/org/oppia/android/scripts/common:repository_file",
        "//scripts/src/java/org/oppia/android/scripts/proto:changed_files_java_proto",
    ],
)

kt_jvm_library(
    name = "retrieve_changed_files_lib",
    testonly = True,
    srcs = [
        "RetrieveChangedFiles.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//scripts/src/java/org/oppia/android/scripts/proto:changed_files_java_proto",
        "//scripts/src/java/org/oppia/android/scripts/proto:script_exemptions_java_proto",
    ],
)
