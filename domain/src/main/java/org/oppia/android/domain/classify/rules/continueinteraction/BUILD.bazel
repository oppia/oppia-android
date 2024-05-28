"""
Classifiers for the 'Continue' interaction.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "continue_module",
    srcs = [
        "ContinueModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/classify:rule_classifier",
        "//domain/src/main/java/org/oppia/android/domain/classify/rules:rule_classifier_provider",
    ],
)
