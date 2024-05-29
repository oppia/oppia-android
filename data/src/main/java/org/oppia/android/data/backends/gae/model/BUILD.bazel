# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
Library for providing model definitions for Oppia's App Engine backend endpoints.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "model",
    srcs = [
        "GaeFeedbackReport.kt",
        "GaeFeedbackReportingAppContext.kt",
        "GaeFeedbackReportingDeviceContext.kt",
        "GaeFeedbackReportingEntryPoint.kt",
        "GaeFeedbackReportingSystemContext.kt",
        "GaeUserSuppliedFeedback.kt",
    ],
    custom_package = "org.oppia.android.data.backends.gae.model",
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:moshi",
    ],
)
