"""
Tests corresponding to script test-only utility libraries.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "TestBazelWorkspaceTest",
    srcs = ["TestBazelWorkspaceTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/testing:test_bazel_workspace",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)

kt_jvm_test(
    name = "TestGitRepositoryTest",
    srcs = ["TestGitRepositoryTest.kt"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/testing:test_git_repository",
        "//testing:assertion_helpers",
        "//third_party:com_google_truth_truth",
        "//third_party:org_jetbrains_kotlin_kotlin-test-junit",
    ],
)
