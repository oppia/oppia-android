"""
Provides a macro for setting up support to use Skylib macros.
"""

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

def set_up():
    """Adds support for using Skylib macros to the workspace.
    """

    # Add support for Skylib: https://github.com/bazelbuild/bazel-skylib#getting-started.
    bazel_skylib_workspace()
