"""
Tests corresponding to test-only utilities that correspond to common script utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "FakeCommandExecutorTest",
    srcs = ["FakeCommandExecutorTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common/testing:fake_command_executor",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
    ],
)
