"""
Test-only utilities for exploration management & loading domain components.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "fake_exploration_retriever",
    testonly = True,
    srcs = [
        "FakeExplorationRetriever.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        "//domain",
    ],
)

kt_android_library(
    name = "test_module",
    testonly = True,
    srcs = [
        "ExplorationStorageTestModule.kt",
    ],
    visibility = ["//:oppia_testing_visibility"],
    deps = [
        ":fake_exploration_retriever",
        "//:dagger",
    ],
)
