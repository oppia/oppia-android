"""
Package for test-only Firebase utilities.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "test_authentication_module",
    testonly = True,
    srcs = [
        "FakeFirebaseAuthWrapperImpl.kt",
        "TestAuthenticationModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//:dagger",
        "//domain/src/main/java/org/oppia/android/domain/auth:firebase_auth_wrapper",
    ],
)
