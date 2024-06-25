"""
See toolchains_step1.bzl for documentation.
"""

load("//third_party/tools/android:toolchain_setup.bzl", set_up_android = "set_up")
load("//third_party/tools/android_test_support:toolchain_setup.bzl", set_up_android_test_support = "set_up")
load("//third_party/tools/java:toolchain_setup.bzl", set_up_java = "set_up")
load("//third_party/tools/kotlin:toolchain_setup_step2.bzl", set_up_kotlin = "set_up")
load("//third_party/tools/oppia_proto_api:toolchain_setup.bzl", set_up_oppia_proto_api = "set_up")
load("//third_party/tools/robolectric:toolchain_setup.bzl", set_up_robolectric = "set_up")
load("//third_party/tools/rules_proto:toolchain_setup_step2.bzl", set_up_rules_proto = "set_up")
load("//third_party/tools/skylib:toolchain_setup.bzl", set_up_skylib = "set_up")
load("//third_party/tools/tools_android:toolchain_setup.bzl", set_up_tools_android = "set_up")

# buildifier: disable=unnamed-macro
def initialize_toolchains_for_workspace_step2():
    """
    See initialize_toolchains_for_workspace_step1 in toolchains_step1.bzl.

    This performs step 2 initialization.
    """

    # Note that the order matters here since toolchains & libraries may have cross-dependencies.
    set_up_skylib()
    set_up_android("androidsdk")
    set_up_oppia_proto_api()
    set_up_kotlin()
    set_up_java()
    set_up_rules_proto()
    set_up_robolectric()
    set_up_tools_android()
    set_up_android_test_support()
