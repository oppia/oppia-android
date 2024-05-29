# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
Library for providing a network API interface with Oppia's App Engine backend.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "api",
    srcs = [
        "FeedbackReportingService.kt",
        "PlatformParameterService.kt",
    ],
    custom_package = "org.oppia.android.data.backends.gae.api",
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//data/src/main/java/org/oppia/android/data/backends/gae/model",
        "//third_party:com_squareup_retrofit2_retrofit",
    ],
)
