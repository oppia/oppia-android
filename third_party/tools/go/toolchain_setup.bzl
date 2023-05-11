"""
Provides a macro for setting up support Buf lint tests.
"""

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("//scripts/third_party/versions:direct_http_versions.bzl", "GO_VERSION")

# buildifier: disable=unnamed-macro
def set_up():
    """Adds support for defining Buf lint tests to the workspace.
    """
    go_rules_dependencies()
    go_register_toolchains(version = GO_VERSION)
