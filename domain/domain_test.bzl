"""
Macros for domain layer tests.
"""

load("//:oppia_android_test.bzl", "oppia_android_layer_level_test")

# TODO(#1620): Remove layer-specific test macros.
def domain_test(name, filtered_tests, deps):
    """
    Creates individual tests for test files in the domain layer.

    Args:
        name: str. The relative path to the Kotlin test file.
        filtered_tests: list of str. The test files that should not have tests defined for them.
        deps: list of str. The list of dependencies needed to build and run this test.
    """

    oppia_android_layer_level_test(
        name = name,
        filtered_tests = filtered_tests,
        deps = deps,
        custom_package = "org.oppia.android.domain",
        test_manifest = "src/test/AndroidManifest.xml",
    )
