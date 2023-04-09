"""
Provides a macro for setting up support to use Robolectric in Java-based targets.
"""

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

def set_up():
    """Adds support for using Robolectric in Java-based targets to the workspace.
    """

    # Add support for Robolectric: https://github.com/robolectric/robolectric-bazel.
    robolectric_repositories()
