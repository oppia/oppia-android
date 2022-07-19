"""
Package for utility libraries that aid in script-related test suites by performing common test
arrangement or operations.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "test_bazel_workspace",
    testonly = True,
    srcs = [
        "TestBazelWorkspace.kt",
    ],
    visibility = ["//scripts:oppia_script_test_visibility"],
    deps = [
        "//third_party:com_google_truth_truth",
    ],
)

kt_jvm_library(
    name = "test_git_repository",
    testonly = True,
    srcs = [
        "TestGitRepository.kt",
    ],
    visibility = ["//scripts:oppia_script_test_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:command_executor",
        "//third_party:com_google_truth_truth",
    ],
)
