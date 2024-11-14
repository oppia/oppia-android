"""
This package contains the root application definitions for alpha builds of the app.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "alpha_application",
    srcs = [
        "AlphaApplicationComponent.kt",
        "AlphaOppiaApplication.kt",
    ],
    visibility = ["//:oppia_binary_visibility"],
    deps = [
        ":alpha_build_flavor_module",
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

kt_android_library(
    name = "alpha_build_flavor_module",
    srcs = [
        "AlphaBuildFlavorModule.kt",
    ],
    visibility = [
        "//app/src/test/java/org/oppia/android/app/application/alpha:__pkg__",
    ],
    deps = [
        "//:dagger",
        "//model/src/main/proto:version_java_proto_lite",
    ],
)
