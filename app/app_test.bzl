"""
Macros for app module tests.
"""

load("//:oppia_android_test.bzl", "oppia_android_module_level_test")

# TODO(#1620): Remove module-specific test macros once Gradle is removed
def app_test(name, processed_src, test_path_prefix, filtered_tests, deps, **kwargs):
    """
    Creates individual tests for test files in the app module.

    Args:
        name: str. The relative path to the Kotlin test file.
        processed_src: str. The source to a processed version of the test that should be used
            instead of the original.
        test_path_prefix: str. The prefix of the test path (which is used to extract the qualified
            class name of the test suite).
        filtered_tests: list of str. The test files that should not have tests defined for them.
        deps: list of str. The list of dependencies needed to build and run this test.
        **kwargs: additional parameters passed in.
    """
    oppia_android_module_level_test(
        name = name,
        processed_src = processed_src,
        filtered_tests = filtered_tests,
        test_path_prefix = test_path_prefix,
        deps = deps,
        custom_package = "org.oppia.android.app.test",
        test_manifest = "src/test/AndroidManifest.xml",
        additional_srcs = ["src/test/java/DataBinderMapperImpl.java"],
        enable_data_binding = True,
        **kwargs
    )
