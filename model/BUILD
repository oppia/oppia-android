load("@rules_java//java:defs.bzl", "java_library")

package(
    default_visibility = ["//app:__subpackages__"],
)

# TODO(#59): Reference needed protos directly elsewhere.
java_library(
    name = "model",
    exports = [
        "//model/src/main/proto:exploration_java_lite_proto",
        "//model/src/main/proto:interaction_object_java_lite_proto",
        "//model/src/main/proto:profile_java_lite_proto",
        "//model/src/main/proto:question_java_lite_proto",
        "//model/src/main/proto:subtitled_html_java_lite_proto",
        "//model/src/main/proto:thumbnail_java_lite_proto",
        "//model/src/main/proto:topic_java_lite_proto",
        "//model/src/main/proto:translation_java_lite_proto",
        "//model/src/main/proto:voiceover_java_lite_proto",
    ],
)
