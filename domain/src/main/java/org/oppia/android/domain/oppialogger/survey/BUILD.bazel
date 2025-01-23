"""
Library for providing logging functionality in a survey.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "survey_events_logger",
    srcs = ["SurveyEventsLogger.kt"],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/oppialogger:oppia_logger",
        "//model/src/main/proto:survey_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
    ],
)
