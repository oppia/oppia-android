"""
Provides a macro for setting up support building Java binary & library targets.
"""

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")

def set_up():
    """Adds support for building Java binary & library targets to the workspace.
    """

    rules_java_dependencies()
    rules_java_toolchains()
