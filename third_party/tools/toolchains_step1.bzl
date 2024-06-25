"""
Defines Starlark macros that are used to set up dependency toolchains needed to build the Oppia
Android project.
"""

load("//third_party/tools/kotlin:toolchain_setup_step1.bzl", set_up_kotlin = "set_up")
load("//third_party/tools/protobuf:toolchain_setup.bzl", set_up_protobuf = "set_up")
load("//third_party/tools/rules_proto:toolchain_setup_step1.bzl", set_up_rules_proto = "set_up")

# buildifier: disable=unnamed-macro
def initialize_toolchains_for_workspace_step1():
    """
    Initializes the toolchains needed to be able to build the Oppia Android app & tests.

    Note that this must be called after loading in this toolchains file, for example:
        load("//third_party/tools:toolchains_step1.bzl", "initialize_toolchains_for_workspace_step1")
        initialize_toolchains_for_workspace_step1()

    Initialization is broken into multiple steps:
    - Step 1 corresponds to early and bootstrap initialization.
    - Step 2 initializes most toolchains completely.
    - Step 3 provides support for some late toolchain initialization.

    Note also that this can't be called until the dependencies themselves are loaded (see
    //third_party/macros/direct_dep_downloader.bzl).
    """

    # Note that the order matters here since toolchains & libraries may have cross-dependencies.
    set_up_kotlin()
    set_up_protobuf()
    set_up_rules_proto()
