"""
Instrumentation macros to define up end-to-end tests.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_instrumentation_test(
        name,
        srcs,
        deps):
    """
    Creates library, binary, and instrumentation test for each test suite.

    Args:
        name: str. The class name of the test suite.
        srcs: list of str. List of test files corresponding to this test suite.
        deps: list of str. The list of dependencies needed to build and run the test.
    """
    kt_android_library(
        name = "%s_lib" % name,
        testonly = True,
        srcs = srcs,
        deps = deps,
    )

    native.android_binary(
        name = "%sBinary" % name,
        testonly = True,
        custom_package = "org.oppia.android",
        instruments = "//instrumentation:oppia_test",
        manifest = "//instrumentation:src/javatests/AndroidManifest.xml",
        deps = [":%s_lib" % name],
    )

    # TODO(#3617): Target isn't supported yet. Remove the manual tag once fixed.
    native.android_instrumentation_test(
        name = name,
        target_device = "@android_test_support//tools/android/emulated_devices/generic_phone:android_23_x86_qemu2",
        test_app = ":%sBinary" % name,
        tags = ["manual"],
    )
