"""
Tests corresponding to wiki-related checks.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "WikiTableOfContentsCheckTest",
    srcs = ["WikiTableOfContentsCheckTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/wiki:wiki_table_of_contents_check_lib",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)
