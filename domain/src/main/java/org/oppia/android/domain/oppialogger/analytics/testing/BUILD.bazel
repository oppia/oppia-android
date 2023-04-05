"""
Package for testing utilities for log uploading functionality.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "fake_log_scheduler",
    testonly = True,
    srcs = [
        "FakeLogScheduler.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/logging:log_uploader",
        "//utility/src/main/java/org/oppia/android/util/logging:metric_log_scheduler",
    ],
)
