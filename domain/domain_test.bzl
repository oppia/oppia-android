"""
Macros for domain module tests.
"""

load("//:oppia_android_test.bzl", "oppia_android_module_level_test")

# TODO(#1620): Remove module-specific test macros once Gradle is removed
def domain_test(name, filtered_tests, deps):
    """
    Creates individual tests for test files in the domain module.

    Args:
        name: str. The relative path to the Kotlin test file.
        filtered_tests: list of str. The test files that should not have tests defined for them.
        deps: list of str. The list of dependencies needed to build and run this test.
    """
    oppia_android_module_level_test(
        name = name,
        filtered_tests = filtered_tests,
        deps = deps,
        custom_package = "org.oppia.android.domain",
        test_manifest = "src/test/AndroidManifest.xml",
        assets = native.glob(["src/main/assets/**"]),
        assets_dir = "src/main/assets/",
    )
