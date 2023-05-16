"""
Defines Starlark macros that are used to set up dependency toolchains needed to build the Oppia
Android project.
"""

load("//third_party/tools/android:toolchain_setup.bzl", set_up_android = "set_up")
load("//third_party/tools/java:toolchain_setup.bzl", set_up_java = "set_up")
load("//third_party/tools/kotlin:toolchain_setup.bzl", set_up_kotlin = "set_up")
load("//third_party/tools/proto:toolchain_setup.bzl", set_up_proto = "set_up")
load("//third_party/tools/robolectric:toolchain_setup.bzl", set_up_robolectric = "set_up")
load("//third_party/tools/skylib:toolchain_setup.bzl", set_up_skylib = "set_up")
load("//third_party/tools/tools_android:toolchain_setup.bzl", set_up_tools_android = "set_up")

# buildifier: disable=unnamed-macro
def initialize_toolchains_for_workspace():
    """
    Initializes the toolchains needed to be able to build the Oppia Android app & tests.

    Note that this must be called after loading in this toolchains file, for example:
        load("//third_party/tools:toolchains.bzl", "initialize_toolchains_for_workspace")
        initialize_toolchains_for_workspace()

    Note also that this can't be called until the dependencies themselves are loaded (see
    //third_party/macros/direct_dep_downloader.bzl).
    """

    # Note that the order matters here since toolchains & libraries may have cross-dependencies.
    set_up_skylib()
    set_up_android("androidsdk")
    set_up_kotlin()
    set_up_java()
    set_up_proto()
    set_up_robolectric()
    set_up_tools_android()
