"""
Provides a macro for setting up support building Android app & library targets.
"""

load("//defs:build_vars.bzl", "BUILD_SDK_VERSION", "BUILD_TOOLS_VERSION")

def set_up(name):
    """Adds support for building Android targets to the workspace.

    Args:
        name: str. The workspace name under which to configure the Android SDK.
    """

    # Android SDK configuration. For more details, see:
    # https://docs.bazel.build/versions/master/be/android.html#android_sdk_repository.
    # TODO(#1542): Sync Android SDK version with the manifest.
    native.android_sdk_repository(
        name = name,
        api_level = BUILD_SDK_VERSION,
        build_tools_version = BUILD_TOOLS_VERSION,
    )

    native.bind(
        name = "databinding_annotation_processor",
        actual = "//third_party/tools/android:compiler_annotation_processor",
    )
