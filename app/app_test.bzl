"""
Macros for app module tests.
"""

load("//:oppia_android_test.bzl", "oppia_android_module_level_test")

# TODO(#1620): Remove module-specific test macros once Gradle is removed
def app_test(
        name,
        deps,
        processed_src = None,
        test_class = None,
        test_path_prefix = None,
        filtered_tests = [],
        **kwargs):
    """
    Creates individual tests for test files in the app module.

    Args:
        name: str. The relative path to the Kotlin test file, or the name of the suite.
        processed_src: str|None. The source to a processed version of the test that should be used
            instead of the original.
        test_class: str|None. The fully qualified test class that will be run (relative to
            src/test/java).
        test_path_prefix: str|None. The prefix of the test path (which is used to extract the
            qualified class name of the test suite).
        deps: list of str. The list of dependencies needed to build and run this test.
        filtered_tests: list of str. The test files that should not have tests defined for them.
        **kwargs: additional parameters passed in.
    """
    oppia_android_module_level_test(
        name = name,
        filtered_tests = filtered_tests,
        deps = deps,
        processed_src = processed_src,
        test_class = test_class,
        test_path_prefix = test_path_prefix,
        custom_package = "org.oppia.android.app.test",
        test_manifest = "//app:test_manifest",
        additional_srcs = ["//app:data_binder_mapper_impl"],
        enable_data_binding = True,
        **kwargs
    )
