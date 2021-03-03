"""
Macros for domain testing tests.
"""

load("//:oppia_android_test.bzl", "oppia_android_module_level_test")

# TODO(#1620): Remove module-specific test macros once Gradle is removed
def testing_test(name, filtered_tests, deps):
    """
    Creates individual tests for test files in the testing module.

    Args:
        name: str. The relative path to the Kotlin test file.
        filtered_tests: list of str. The test files that should not have tests defined for them.
        deps: list of str. The list of dependencies needed to build and run this test.
    """
    oppia_android_module_level_test(
        name = name,
        filtered_tests = filtered_tests,
        deps = deps,
        custom_package = "org.oppia.android.testing",
        test_manifest = "src/test/AndroidManifest.xml",
    )
