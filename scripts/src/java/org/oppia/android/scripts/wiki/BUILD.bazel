"""
Libraries corresponding to scripting tools that help with continuous integration workflows.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "wiki_table_of_contents_check_lib",
    testonly = True,
    srcs = [
        "WikiTableOfContentsCheck.kt",
    ],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    deps = [
        "//scripts/src/java/org/oppia/android/scripts/common:bazel_client",
        "//scripts/src/java/org/oppia/android/scripts/common:git_client",
    ],
)
