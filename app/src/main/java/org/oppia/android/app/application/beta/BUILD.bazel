"""
This package contains the root application definitions for beta builds of the app.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "beta_application",
    srcs = [
        "BetaApplicationComponent.kt",
        "BetaBuildFlavorModule.kt",
        "BetaOppiaApplication.kt",
    ],
    visibility = [
        "//:oppia_binary_visibility",
        "//app/src/test/java/org/oppia/android/app/application/beta:__pkg__",
    ],
    deps = [
        "//:dagger",
        "//app",
        "//app/src/main/java/org/oppia/android/app/application:abstract_application",
        "//app/src/main/java/org/oppia/android/app/application:application_component",
        "//app/src/main/java/org/oppia/android/app/application:common_application_modules",
        "//domain/src/main/java/org/oppia/android/domain/auth:auth_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:prod_module",
        "//utility/src/main/java/org/oppia/android/util/networking:prod_module",
    ],
)
