"""
Libraries corresponding to telemetry scripts, including tools for locally analyzing event data.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "decode_user_study_event_string_lib",
    testonly = True,
    srcs = ["DecodeUserStudyEventString.kt"],
    visibility = ["//scripts:oppia_script_binary_visibility"],
    runtime_deps = [
        "//third_party:org_eclipse_parsson_parsson",
        "//third_party:org_snakeyaml_snakeyaml-engine",
    ],
    deps = [
        "//model/src/main/proto:event_logger_java_proto",
        "//third_party:com_google_protobuf_protobuf-java",
        "//third_party:com_google_protobuf_protobuf-java-util",
        "//third_party:io_xlate_yaml-json",
        "//third_party:jakarta_json_jakarta_json-api",
    ],
)
