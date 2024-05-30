"""
Testing utilities for languages & locales.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "test_activity_recreator",
    testonly = True,
    srcs = [
        "TestActivityRecreator.kt",
    ],
    visibility = [
        "//app:app_testing_visibility",
    ],
    deps = [
        "//app/src/main/java/org/oppia/android/app/translation:activity_recreator",
        "//third_party:javax_inject_javax_inject",
    ],
)

kt_android_library(
    name = "test_module",
    testonly = True,
    srcs = [
        "ActivityRecreatorTestModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":test_activity_recreator",
        "//:dagger",
        "//app/src/main/java/org/oppia/android/app/translation:activity_recreator",
    ],
)
