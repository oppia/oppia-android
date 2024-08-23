"""
Libraries corresponding to developer scripts that obtain coverage data for test targets.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "run_coverage_lib",
    testonly = True,
    srcs = [
        "RunCoverage.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        ":coverage_runner",
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/common:proto_string_encoder",
        "//scripts/src/java/org/oppia/android/scripts/coverage/reporter:coverage_reporter_lib",
        "//scripts/src/java/org/oppia/android/scripts/proto:script_exemptions_java_proto",
    ],
)

kt_jvm_library(
    name = "coverage_runner",
    testonly = True,
    srcs = [
        "CoverageRunner.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/proto:coverage_java_proto",
    ],
)
