# TODO(BenHenning): Restrict visibility once this is broken into smaller packages.
package(
    default_visibility = ["//visibility:public"],
)

load("@rules_proto//proto:defs.bzl", "proto_library", "proto_lang_toolchain")

# Set up Bazel to use protolite for protobuf generation.
proto_lang_toolchain(
    name = "javalite_toolchain",
    command_line = "--$(PLUGIN_OUT)=shared,immutable:$(OUT)",
    plugin = ":javalite_plugin",
    runtime = ":protobuf_lite",
)

# TODO(BenHenning): Break this library into smaller pieces.
proto_library(
  name = "model_lib",
  srcs = glob(["*.proto"]),
)
