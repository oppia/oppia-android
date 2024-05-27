# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
Library for providing test-only HTTP mocks for the app's networking layer.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "network",
    testonly = True,
    srcs = [
        "MockFeedbackReportingService.kt",
        "MockPlatformParameterService.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":impl",
        "//data/src/main/java/org/oppia/android/data/backends/gae:network_interceptors",
        "//data/src/main/java/org/oppia/android/data/backends/gae/api",
        "//data/src/main/java/org/oppia/android/data/backends/gae/model",
        "//testing/src/main/java/org/oppia/android/testing/platformparameter:test_constants",
        "//third_party:com_squareup_retrofit2_retrofit-mock",
        "//utility",
    ],
)

kt_android_library(
    name = "impl",
    testonly = True,
    srcs = [
        "ApiMockLoader.kt",
    ],
    deps = [
        "//third_party:androidx_test_core",
    ],
)

kt_android_library(
    name = "test_module",
    testonly = True,
    srcs = [
        "RetrofitTestModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//:dagger",
        "//data/src/main/java/org/oppia/android/data/backends/gae:oppia_retrofit",
        "//data/src/main/java/org/oppia/android/data/backends/gae:prod_module",
        "//third_party:com_squareup_retrofit2_retrofit-mock",
    ],
)
