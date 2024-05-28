"""
Testing utilities for language & locale general utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "test_oppia_bidi_formatter",
    testonly = True,
    srcs = [
        "TestOppiaBidiFormatter.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//utility/src/main/java/org/oppia/android/util/locale:impl",
        "//utility/src/main/java/org/oppia/android/util/locale:oppia_bidi_formatter",
    ],
)

kt_android_library(
    name = "test_module",
    testonly = True,
    srcs = [
        "LocaleTestModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":test_oppia_bidi_formatter",
        "//:dagger",
        "//utility/src/main/java/org/oppia/android/util/locale:impl",
    ],
)
