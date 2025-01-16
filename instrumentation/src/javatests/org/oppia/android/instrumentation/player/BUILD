"""
This library contains the test suite source files of the player package.
"""

load("//instrumentation:oppia_instrumentation_test.bzl", "oppia_instrumentation_test")

oppia_instrumentation_test(
    name = "ExplorationPlayerTest",
    srcs = [
        "ExplorationPlayerTest.kt",
    ],
    deps = [
        "//instrumentation/src/java/org/oppia/android/instrumentation/testing:e2e_test_helper",
        "//third_party:androidx_test_ext_junit",
        "//third_party:androidx_test_runner",
        "//third_party:androidx_test_uiautomator_uiautomator",
        "//third_party:com_google_truth_truth",
        "//third_party:junit_junit",
    ],
)
