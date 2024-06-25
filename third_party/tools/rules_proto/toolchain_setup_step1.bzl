"""
Provides a macro for setting up support building proto library targets.
"""

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies")

#load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

# buildifier: disable=unnamed-macro
def set_up():
    """Adds support for building proto library targets to the workspace.
    """

    rules_proto_dependencies()
