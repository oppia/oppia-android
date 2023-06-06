"""
Utilities corresponding to simplify using Jetpack's view model functionality.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "observable_array_list",
    srcs = [
        "ObservableArrayList.kt",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:androidx_databinding_databinding-adapters",
    ],
)

kt_android_library(
    name = "observable_view_model",
    srcs = [
        "ObservableViewModel.kt",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:androidx_databinding_databinding-adapters",
    ],
)
