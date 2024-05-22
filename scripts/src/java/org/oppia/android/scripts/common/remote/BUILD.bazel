"""
Remote service endpoints for common shared utilities, such as for GitHub API integration.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "github_service",
    testonly = True,
    srcs = ["GitHubService.kt"],
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common/model:github_issue",
        "//third_party:com_squareup_retrofit2_retrofit",
    ],
)
