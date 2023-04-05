"""
Provides a macro for setting up support building Android app & library targets.
"""

load("//:build_vars.bzl", "BUILD_SDK_VERSION", "BUILD_TOOLS_VERSION")

def setUp():
    """
    Adds support for building Android targets to the workspace.
    """

    # Android SDK configuration. For more details, see:
    # https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository.
    # TODO(#1542): Sync Android SDK version with the manifest.
    native.android_sdk_repository(
        name = "androidsdk",
        api_level = BUILD_SDK_VERSION,
        build_tools_version = BUILD_TOOLS_VERSION,
    )

    native.bind(
        name = "databinding_annotation_processor",
        actual = "//tools/android:compiler_annotation_processor",
    )
