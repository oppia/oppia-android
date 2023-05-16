"""
Central macros pertaining to setting up tests across the codebase.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//defs:build_vars.bzl", "MIN_SDK_VERSION")
load(":generate_android_manifest.bzl", "generate_android_manifest")

# TODO(#4748): Move this to SDK 31 & use the production target by default, and update docs below.
_TEST_TARGET_SDK_VERSION = 30

def oppia_android_test(
        name,
        srcs,
        deps,
        android_merge_deps = [],
        include_robolectric_support = True,
        runtime_deps = []):
    """Creates a local Oppia test target with Kotlin support.

    Note a few things that are important to know when defining these tests:
    - This macro has very similar behavior to oppia_android_library(), so it's suggested to be
        well-versed in the behavior of that macro before using this one.
    - Tests should expect a default resource package of "org.oppia.android" which means that most
        resources that they use will be available via "org.oppia.android.R".
    - An additional, internal library is created and then wrapped in android_local_test() for actual
        execution. This may impact queries and/or build patterns for test targets.
    - The test will create an automatically generated manifest for resource processing. This
        manifest will default the test's minimum SDK to that of the production app. The target SDK
        is currently limited to SDK 30 for now.
    - The test target is an android_local_test() target which means it cannot be run on an emulator
        or physical device.

    Args:
        name: str. The name of the test target being defined. Conventionally, this is same as the
            name of the test suite's primary file without its extension. Unlike other libraries,
            tests are named based on their test file rather than using snake_case convention.
        srcs: list of label. The sources to compile into this test. Conventionally, this is just a
            single test file.
        deps: list of label. The Java, Kotlin, or Android dependencies that this library requires to
            build and run. This is expected to always be required even for simple tests (since the
            test needs to at least include the dependency of the target that it's testing).
        temp_test_class_migration: str. A temporary parameter used to specify the test class for
            suites that are using a shared test library. This turns off library generation and
            significantly changes the behavior of this macro. This should *not* be used for new
            code as it's meant only for legacy combined test suites that will eventually be split
            up. This defaults to None.
        android_merge_deps: list of label. The Android dependencies that this test requires
            resources and/or assets from (but not code--those dependencies should go in 'deps').
            This defaults to empty list.
        include_robolectric_support: boolean. Whether to automatically include Robolectric library
            support. This defaults to 'True'.
        runtime_deps: list of label. Additional dependencies that are only required by the test at
            runtime. Move dependencies here only if they are needed for the test suite to pass and
            are suggested for removal by rules_kotlin's unused dependency checker. This is rare as
            nearly all dependencies have direct references in test code and can go in the regular
            'deps' list. This defaults to empty list.
    """

    # TODO(#59): Remove the forced resources package once Gradle is removed since tests can then use
    # locally merged resources.
    resources_package = "org.oppia.android"
    test_manifest = _generate_test_android_manifest(name, package = resources_package)
    include_support_for_android_resources = len(android_merge_deps) != 0

    kt_android_library(
        name = name + "_lib",
        custom_package = resources_package if include_support_for_android_resources else None,
        srcs = srcs,
        deps = deps,
        testonly = True,
        manifest = test_manifest if include_support_for_android_resources else None,
        android_merge_deps = android_merge_deps,
    )

    native.android_local_test(
        name = name,
        manifest = test_manifest,
        manifest_values = {
            "applicationId": resources_package,
            "minSdkVersion": "%s" % MIN_SDK_VERSION,
            "targetSdkVersion": "%s" % _TEST_TARGET_SDK_VERSION,
            "versionCode": "0",
            "versionName": "0.1-test",
        },
        deps = [":%s_lib" % name] + deps + ([
            "//third_party:robolectric_android-all",
        ] if include_robolectric_support else []),
        runtime_deps = runtime_deps + ([
            "//third_party:org_robolectric_robolectric",
        ] if include_robolectric_support else []),
    )

def _generate_test_android_manifest(name, package):
    return generate_android_manifest(
        name,
        package,
        min_sdk_version = MIN_SDK_VERSION,
        target_sdk_version = _TEST_TARGET_SDK_VERSION,
    )
