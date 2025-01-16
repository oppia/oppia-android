"""
This library contains the test application and modules used to build the test app for end-to-end tests.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "oppia_test_application",
    testonly = True,
    srcs = [
        "EndToEndTestGcsResourceModule.kt",
        "EndToEndTestImageParsingModule.kt",
        "EndToEndTestNetworkConfigModule.kt",
        "TestApplicationComponent.kt",
        "TestOppiaApplication.kt",
    ],
    visibility = ["//:oppia_e2e_testing_visibility"],
    deps = [
        "//:dagger",
        "//app",
        "//app/src/main/java/org/oppia/android/app/application:abstract_application",
        "//app/src/main/java/org/oppia/android/app/application:application_component",
        "//app/src/main/java/org/oppia/android/app/application:common_application_modules",
        "//app/src/main/java/org/oppia/android/app/application/testing:testing_build_flavor_module",
        "//data/src/main/java/org/oppia/android/data/backends/gae:network_config_annotations",
        "//domain",
        "//testing/src/main/java/org/oppia/android/testing/firebase:test_authentication_module",
        "//utility",
        "//utility/src/main/java/org/oppia/android/util/logging/firebase:debug_module",
        "//utility/src/main/java/org/oppia/android/util/networking:debug_module",
    ],
)
