"""
Domain services & definitions corresponding to managing the system clipboard when copying & pasting
text.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "clipboard_controller",
    srcs = [
        "ClipboardController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//utility/src/main/java/org/oppia/android/util/data:data_provider",
        "//utility/src/main/java/org/oppia/android/util/data:data_providers",
    ],
)
