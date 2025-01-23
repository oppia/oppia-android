"""
This library contains all protos used in the scripts module.

In Bazel, proto files are built using the oppia_proto_library() and java_proto_library() rules. The
oppia_proto_library() rule creates a proto file library to be used in multiple languages. The
java_proto_library() rule takes in a proto_library target and generates java code. For more context
on adding a new proto library, please refer to //model/BUILD.bazel.
"""

load("@rules_java//java:defs.bzl", "java_proto_library")
load("//model:oppia_proto_library.bzl", "oppia_proto_library")

oppia_proto_library(
    name = "affected_tests_proto",
    srcs = ["affected_tests.proto"],
)

java_proto_library(
    name = "affected_tests_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":affected_tests_proto"],
)

oppia_proto_library(
    name = "changed_files_proto",
    srcs = ["changed_files.proto"],
)

java_proto_library(
    name = "changed_files_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":changed_files_proto"],
)

oppia_proto_library(
    name = "coverage_proto",
    srcs = ["coverage.proto"],
)

java_proto_library(
    name = "coverage_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":coverage_proto"],
)

oppia_proto_library(
    name = "filename_pattern_validation_checks_proto",
    srcs = ["filename_pattern_validation_checks.proto"],
    visibility = ["//scripts:oppia_script_binary_visibility"],
)

java_proto_library(
    name = "filename_pattern_validation_checks_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":filename_pattern_validation_checks_proto"],
)

oppia_proto_library(
    name = "file_content_validation_checks_proto",
    srcs = ["file_content_validation_checks.proto"],
    visibility = ["//scripts:oppia_script_binary_visibility"],
)

java_proto_library(
    name = "file_content_validation_checks_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":file_content_validation_checks_proto"],
)

oppia_proto_library(
    name = "script_exemptions_proto",
    srcs = ["script_exemptions.proto"],
    visibility = ["//scripts:oppia_script_binary_visibility"],
)

java_proto_library(
    name = "script_exemptions_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":script_exemptions_proto"],
)

oppia_proto_library(
    name = "maven_dependencies_proto",
    srcs = ["maven_dependencies.proto"],
    visibility = ["//scripts:oppia_script_binary_visibility"],
)

java_proto_library(
    name = "maven_dependencies_java_proto",
    visibility = ["//scripts:oppia_script_library_visibility"],
    deps = [":maven_dependencies_proto"],
)
