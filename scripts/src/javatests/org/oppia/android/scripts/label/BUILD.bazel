"""
Tests corresponding to accessibility label check that ensures all the activities in the codebase
are defined with labels.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "AccessibilityLabelCheckTest",
    srcs = ["AccessibilityLabelCheckTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/label:accessibility_label_check_lib",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)
