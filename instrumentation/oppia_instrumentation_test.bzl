"""
Instrumentation macros to setup up end-to-end tests.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

def oppia_instrumentation_test(
        name,
        srcs,
        deps):
    """
    Creates library, binary and a instrumentation test for each test suite.

    Args:
        name: str. The class name of the Test suite or the test file.
        srcs: list of str. List of test files under instrumentation module.
        deps: list of str. The list of dependencies needed to build and run the library.
    """
    kt_android_library(
        name = name + "_lib",
        testonly = True,
        srcs = srcs,
        visibility = ["//:oppia_testing_visibility"],
        deps = deps,
    )

    native.android_binary(
        name = name + "Binary",
        testonly = True,
        custom_package = "org.oppia.android",
        instruments = "//:oppia_test",
        manifest = "src/javatest/AndroidManifest.xml",
        deps = [":" + name + "_lib"],
    )

    native.android_instrumentation_test(
        name = name,
        target_device = "@android_test_support//tools/android/emulated_devices/generic_phone:android_23_x86_qemu2",
        test_app = ":" + name + "Binary",
    )
