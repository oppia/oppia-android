"""
Domain services & definitions corresponding to managing ephemeral states in play sessions.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

kt_android_library(
    name = "state_deck",
    srcs = [
        "StateDeck.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:exploration_checkpoint_java_proto_lite",
    ],
)

kt_android_library(
    name = "state_graph",
    srcs = [
        "StateGraph.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:exploration_checkpoint_java_proto_lite",
    ],
)

kt_android_library(
    name = "state_list",
    srcs = [
        "StateList.kt",
    ],
    visibility = ["//:oppia_api_visibility"],
    deps = [
        "//model/src/main/proto:exploration_java_proto_lite",
        "//model/src/main/proto:question_java_proto_lite",
    ],
)
