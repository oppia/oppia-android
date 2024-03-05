"""
Instrumentation macros to define up end-to-end tests.
"""

load("@bazel_skylib//rules:build_test.bzl", "build_test")
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

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
    test_lib_name = "%s_lib" % name
    test_binary_name = "%sBinary" % name

    kt_android_library(
        name = test_lib_name,
        testonly = True,
        srcs = srcs,
        deps = deps,
    )

    native.android_binary(
        name = test_binary_name,
        testonly = True,
        custom_package = "org.oppia.android",
        instruments = "//instrumentation:oppia_test",
        manifest = "//instrumentation:src/javatests/AndroidManifest.xml",
        deps = [test_lib_name],
    )

    # TODO(#3617): Target isn't supported yet. Remove the manual tag once fixed.
    native.android_instrumentation_test(
        name = name,
        target_device = "@android_test_support//tools/android/emulated_devices/generic_phone:android_23_x86_qemu2",
        test_app = test_binary_name,
        tags = ["manual"],
    )

    build_test(
        name = "%s_smoke_test" % name,
        targets = [name, test_binary_name],
    )
