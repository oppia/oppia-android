"""
Package for test-only utilities that correspond to common script utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "fake_command_executor",
    testonly = True,
    srcs = ["FakeCommandExecutor.kt"],
    visibility = ["//scripts:oppia_script_test_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:command_executor",
    ],
)
