"""
General utilities for domain module.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "asset",
    srcs = [
        "JsonAssetRetriever.kt",
    ],
    visibility = ["//domain:__subpackages__"],
    deps = [
        ":extensions",
        "//third_party:javax_inject_javax_inject",
        "//utility/src/main/java/org/oppia/android/util/caching:annotations",
        "//utility/src/main/java/org/oppia/android/util/caching:asset_repository",
    ],
)

kt_android_library(
    name = "extensions",
    srcs = [
        "InteractionObjectExtensions.kt",
        "JsonExtensions.kt",
        "WorkDataExtensions.kt",
    ],
    visibility = ["//domain:__subpackages__"],
    deps = [
        "//model/src/main/proto:question_java_proto_lite",
        "//third_party:androidx_work_work-runtime-ktx",
        "//utility/src/main/java/org/oppia/android/util/math:extensions",
    ],
)

kt_android_library(
    name = "retriever",
    srcs = [
        "StateRetriever.kt",
    ],
    visibility = ["//domain:__subpackages__"],
    deps = [
        ":extensions",
        "//model/src/main/proto:question_java_proto_lite",
        "//third_party:javax_inject_javax_inject",
    ],
)
