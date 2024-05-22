"""
Library for providing authentication with Firebase functionality.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "authentication_controller",
    srcs = [
        "AuthenticationController.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        ":firebase_auth_wrapper",
        "//third_party:javax_inject_javax_inject",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core",
        "//utility/src/main/java/org/oppia/android/util/threading:annotations",
    ],
)

kt_android_library(
    name = "firebase_auth_wrapper",
    srcs = [
        "FirebaseAuthInstance.kt",
        "FirebaseAuthInstanceWrapper.kt",
        "FirebaseAuthWrapper.kt",
        "FirebaseUserWrapper.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:com_google_firebase_firebase-auth-ktx",
        "//third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core",
        "//utility/src/main/java/org/oppia/android/util/data:async_result",
    ],
)

kt_android_library(
    name = "auth_module",
    srcs = [
        "AuthenticationModule.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":authentication_controller",
        ":firebase_auth_wrapper_impl",
        "//:dagger",
    ],
)

kt_android_library(
    name = "firebase_auth_wrapper_impl",
    srcs = [
        "FirebaseAuthWrapperImpl.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":firebase_auth_instance_wrapper_impl",
        "//:dagger",
    ],
)

kt_android_library(
    name = "firebase_auth_instance_wrapper_impl",
    srcs = [
        "FirebaseAuthInstanceWrapperImpl.kt",
    ],
    visibility = ["//:oppia_prod_module_visibility"],
    deps = [
        ":firebase_auth_wrapper",
        "//:dagger",
    ],
)
