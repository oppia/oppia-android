"""
Tests corresponding to developer scripts that help with obtaining coverage data for test targets.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "CoverageReporterTest",
    srcs = ["CoverageReporterTest.kt"],
    test_class = "org.oppia.android.scripts.coverage.reporter.CoverageReporterTest",
    deps = [
        "//scripts:test_file_check_assets",
        "//scripts/src/java/org/oppia/android/scripts/coverage/reporter:coverage_reporter_lib",
        "//scripts/src/java/org/oppia/android/scripts/proto:script_exemptions_java_proto",
        "//testing:assertion_helpers",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)
