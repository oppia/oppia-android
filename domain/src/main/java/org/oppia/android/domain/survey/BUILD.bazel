"""
Library for providing survey functionality in the app.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "gating_controller",
    srcs = [
        "SurveyGatingController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/profile:profile_management_controller",
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/locale:oppia_locale",
        "//utility/src/main/java/org/oppia/android/util/system:oppia_clock",
    ],
)

kt_android_library(
    name = "survey_controller",
    srcs = [
        "SurveyController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":survey_progress_controller",
    ],
)

kt_android_library(
    name = "survey_progress_controller",
    srcs = [
        "SurveyProgressController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":survey_progress",
        "//domain",
        "//domain/src/main/java/org/oppia/android/domain/profile:profile_management_controller",
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/locale:oppia_locale",
        "//utility/src/main/java/org/oppia/android/util/system:oppia_clock",
    ],
)

kt_android_library(
    name = "survey_progress",
    srcs = [
        "SurveyProgress.kt",
        "SurveyQuestionDeck.kt",
        "SurveyQuestionGraph.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//domain/src/main/java/org/oppia/android/domain/oppialogger/survey:survey_events_logger",
        "//model/src/main/proto:survey_java_proto_lite",
    ],
)
