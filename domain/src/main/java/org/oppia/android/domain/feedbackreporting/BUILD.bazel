"""
Domain services & definitions corresponding to user feedback reporting.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "prod_module",
    srcs = [
        "FeedbackReportingModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":report_schema_version",
        "//:dagger",
    ],
)

kt_android_library(
    name = "report_schema_version",
    srcs = [
        "ReportSchemaVersion.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//third_party:javax_inject_javax_inject",
    ],
)
