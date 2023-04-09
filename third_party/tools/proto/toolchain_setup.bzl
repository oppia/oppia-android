"""
Provides a macro for setting up support building proto library targets.
"""

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

# buildifier: disable=unnamed-macro
def set_up():
    """Adds support for building proto library targets to the workspace.
    """

    # The proto_compiler and proto_java_toolchain bindings load the protos rules needed for building
    # protos while helping us avoid the unnecessary compilation of protoc. References:
    # - https://github.com/google/startup-os/blob/5f30a62/WORKSPACE#L179-L187
    # - https://github.com/bazelbuild/bazel/issues/7095
    native.bind(
        name = "proto_compiler",
        actual = "//third_party/tools:protoc",
    )
    native.bind(
        name = "proto_java_toolchain",
        actual = "//third_party/tools:java_toolchain",
    )

    rules_proto_dependencies()
    rules_proto_toolchains()
