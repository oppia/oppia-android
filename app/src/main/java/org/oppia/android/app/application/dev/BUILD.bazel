"""
This package contains the root application definitions for developer builds of the app.

Note that this will be the application configuration used for Gradle builds of the app. For Bazel,
there are specially defined top-level build flavors which will select their corresponding
application configuration.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "developer_application",
    srcs = [
        "DeveloperApplicationComponent.kt",
        "DeveloperBuildFlavorModule.kt",
        "DeveloperOppiaApplication.kt",
    ],
    visibility = [
        "//:oppia_binary_visibility",
        "//app/src/test/java/org/oppia/android/app/application/dev:__pkg__",
    ],
    deps = [
        "//:dagger",
        "//app",
        "//app/src/main/java/org/oppia/android/app/application:abstract_application",
        "//app/src/main/java/org/oppia/android/app/application:application_component",
        "//app/src/main/java/org/oppia/android/app/application:common_application_modules",
        "//domain/src/main/java/org/oppia/android/domain/auth:auth_module",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)
