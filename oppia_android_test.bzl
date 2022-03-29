"""
Central macros pertaining to setting up tests across the codebase.
"""

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_android_library")

# TODO(#1620): Remove module-specific test macros once Gradle is removed
def oppia_android_module_level_test(
        name,
        filtered_tests,
        deps,
        processed_src = None,
        test_class = None,
        test_path_prefix = "src/test/java/",
        additional_srcs = [],
        **kwargs):
    """
    Creates individual tests for a test file at the module level.

    Args:
        name: str. The relative path to the Kotlin test file.
        filtered_tests: list of str. The test files that should not have tests defined for them.
        deps: list of str. The list of dependencies needed to build and run this test.
        processed_src: str|None. The source to a processed version of the test that should be used
            instead of the original.
        test_class: str|None. The fully qualified test class that will be run (relative to
            src/test/java).
        test_path_prefix: str|None. The prefix of the test path (which is used to extract the
            qualified class name of the test suite).
        additional_srcs: list of str. Additional source files to build into the test binary.
        **kwargs: additional parameters to pass to oppia_android_test.
    """
    if name not in filtered_tests:
        oppia_android_test(
            name = name[:name.find(".kt")] if "/" in name else name,
            srcs = [processed_src or name] + additional_srcs,
            test_class = (
                test_class or _remove_prefix_suffix(name, test_path_prefix, ".kt").replace("/", ".")
            ),
            deps = deps,
            **kwargs
        )

def oppia_android_test(
        name,
        srcs,
        test_manifest,
        custom_package,
        test_class,
        deps,
        enable_data_binding = False,
        assets = None,
        assets_dir = None,
        **kwargs):
    """
    Creates a local Oppia test target with Kotlin support.

    Note that this creates an additional, internal library.

    Args:
      name: str. The name of the Kotlin test file without the '.kt' suffix.
      srcs: list of str. The name of the Kotlin test files to be run.
      test_manifest: str. The path to the test manifest file.
      custom_package: str. The module's package. Example: 'org.oppia.utility'.
      test_class: The package of the src file. For example, if the src is 'FakeEventLoggerTest.kt',
          then the test_class would be "org.oppia.testing.FakeEventLoggerTest".
      enable_data_binding: boolean. Indicates whether the test enables data-binding.
      deps: list of str. The list of dependencies needed to run the tests.
      assets: list of str. A list of assets needed to run the tests.
      assets_dir: str. The path to the assets directory.
      **kwargs: additional parameters to pass to android_local_test.
    """

    kt_android_library(
        name = name + "_lib",
        custom_package = custom_package,
        srcs = srcs,
        deps = deps,
        testonly = True,
        manifest = test_manifest,
        assets = assets,
        assets_dir = assets_dir,
        enable_data_binding = enable_data_binding,
    )
    native.android_local_test(
        name = name,
        custom_package = custom_package,
        test_class = test_class,
        manifest = test_manifest,
        deps = [":" + name + "_lib"] + deps,
        **kwargs
    )

def _remove_prefix_suffix(str, prefix, suffix):
    return str[str.find(prefix) + len(prefix):str.find(suffix)]
