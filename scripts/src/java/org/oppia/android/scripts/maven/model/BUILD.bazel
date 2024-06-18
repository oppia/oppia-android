"""
Data structures corresponding to parsing Maven installation manifests.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "model",
    testonly = True,
    srcs = [
        "MavenArtifact.kt",
        "MavenInstallJson.kt",
    ],
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = ["//third_party:moshi"],
)
