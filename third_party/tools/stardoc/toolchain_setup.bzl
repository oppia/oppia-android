"""
Provides a macro for setting up support for Stardoc dependencies.
"""

load("@io_bazel_stardoc//:setup.bzl", "stardoc_repositories")

def set_up():
    """Adds support for using Stardoc in Bazel macros & dependencies to the workspace.
    """

    # Add Stardoc support per https://github.com/bazelbuild/stardoc/releases/tag/0.5.3.
    stardoc_repositories()
