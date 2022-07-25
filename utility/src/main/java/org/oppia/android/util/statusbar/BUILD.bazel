"""
Utilities for managing the Android status bar.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "status_bar_color",
    srcs = [
        "StatusBarColor.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:androidx_appcompat_appcompat",
    ],
)
