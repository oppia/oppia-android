# Definitions of all protos used in Oppia Android. Reference:
# https://github.com/protocolbuffers/protobuf/blob/d2d6ff/examples/BUILD.

load("@rules_proto//proto:defs.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_lite_proto_library")

# TODO(#59): Move these to their final locations.

package(
    default_visibility = ["//model:__pkg__"],
)

proto_library(
    name = "example_proto",
    testonly = True,
    srcs = ["example.proto"],
)

java_lite_proto_library(
    name = "example_java_lite_proto",
    testonly = True,
    deps = [":example_proto"],
)

proto_library(
    name = "exploration_proto",
    srcs = ["exploration.proto"],
    deps = [
        ":subtitled_html_proto",
        ":interaction_object_proto",
        ":translation_proto",
        ":voiceover_proto",
    ],
)

java_lite_proto_library(
    name = "exploration_java_lite_proto",
    deps = [":exploration_proto"],
)

proto_library(
    name = "interaction_object_proto",
    srcs = ["interaction_object.proto"],
)

java_lite_proto_library(
    name = "interaction_object_java_lite_proto",
    deps = [":interaction_object_proto"],
)

proto_library(
    name = "profile_proto",
    srcs = ["profile.proto"],
)

java_lite_proto_library(
    name = "profile_java_lite_proto",
    deps = [":profile_proto"],
)

proto_library(
    name = "question_proto",
    srcs = ["question.proto"],
    deps = [
        ":exploration_proto",
        ":subtitled_html_proto",
    ],
)

java_lite_proto_library(
    name = "question_java_lite_proto",
    deps = [":question_proto"],
)

proto_library(
    name = "subtitled_html_proto",
    srcs = ["subtitled_html.proto"],
)

java_lite_proto_library(
    name = "subtitled_html_java_lite_proto",
    deps = [":subtitled_html_proto"],
)

proto_library(
    name = "thumbnail_proto",
    srcs = ["thumbnail.proto"],
)

java_lite_proto_library(
    name = "thumbnail_java_lite_proto",
    deps = [":thumbnail_proto"],
)

proto_library(
    name = "topic_proto",
    srcs = ["topic.proto"],
    deps = [
        ":subtitled_html_proto",
        ":thumbnail_proto",
        ":translation_proto",
        ":voiceover_proto",
    ],
)

java_lite_proto_library(
    name = "topic_java_lite_proto",
    deps = [":topic_proto"],
)

proto_library(
    name = "translation_proto",
    srcs = ["translation.proto"],
)

java_lite_proto_library(
    name = "translation_java_lite_proto",
    deps = [":translation_proto"],
)

proto_library(
    name = "voiceover_proto",
    srcs = ["voiceover.proto"],
)

java_lite_proto_library(
    name = "voiceover_java_lite_proto",
    deps = [":voiceover_proto"],
)
