"""
This package contains the root application definitions for general availability builds of the app.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "general_availability_application",
    srcs = [
        "GaApplicationComponent.kt",
        "GaBuildFlavorModule.kt",
        "GaOppiaApplication.kt",
    ],
    visibility = [
        "//:oppia_binary_visibility",
        "//app/src/test/java/org/oppia/android/app/application/ga:__pkg__",
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
