"""
General purposes utilities to manage extensions
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "bundle_extensions",
    srcs = [
        "BundleExtensions.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:com_google_protobuf_protobuf-javalite",
    ],
)

kt_android_library(
    name = "context_extensions",
    srcs = [
        "ContextExtensions.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//third_party:com_google_protobuf_protobuf-javalite",
    ],
)

kt_android_library(
    name = "string_extensions",
    srcs = [
        "StringExtensions.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
)
