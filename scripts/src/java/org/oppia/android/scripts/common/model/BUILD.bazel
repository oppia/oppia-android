"""
Data structures corresponding to common utilities, such as for GitHub API integration.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "github_issue",
    testonly = True,
    srcs = ["GitHubIssue.kt"],
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = ["//third_party:moshi"],
)
