"""
Provides a macro for setting up support for using android_test_support.
"""

load("@android_test_support//:repo.bzl", "android_test_repositories")

def set_up():
    """Adds support for using android_test_support.
    """

    # See https://bazel.build/docs/android-instrumentation-test#workspace-dependencies for context.
    android_test_repositories()
