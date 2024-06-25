"""
See toolchains_step1.bzl for documentation.
"""

load("//third_party/tools/rules_proto:toolchain_setup_step3.bzl", set_up_rules_proto = "set_up")

# buildifier: disable=unnamed-macro
def initialize_toolchains_for_workspace_step3():
    """
    See initialize_toolchains_for_workspace_step1 in toolchains_step1.bzl.

    This performs step 3 initialization.
    """

    # Note that the order matters here since toolchains & libraries may have cross-dependencies.
    set_up_rules_proto()
