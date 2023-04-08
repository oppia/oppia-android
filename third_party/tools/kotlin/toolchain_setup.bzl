"""
Provides a macro for setting up support building Kotlin Android & JVM targets.
"""

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")

def setUp():
    """
    Adds support for building Kotlin targets to the workspace.
    """

    # Add support for Kotlin: https://github.com/bazelbuild/rules_kotlin.
    kotlin_repositories()
    native.register_toolchains("//third_party/tools/kotlin:kotlin_16_jdk9_toolchain")
