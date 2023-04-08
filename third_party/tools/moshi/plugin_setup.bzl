"""
Provides a macro for setting up support for using Moshi in Java target builds.
"""

load("@rules_java//java:defs.bzl", "java_library", "java_plugin")

def setUp(name, visibility, moshi_compiler_dep, moshi_exported_library):
    """
    Defines a new java_library with the specified name and visibility that exposes the provided
    moshi_compiler_dep as an annotation processor-enabled compiler plugin (which means dependencies
    on the new target will run the Moshi annotation processor).
    """
    java_plugin(
        name = "%s_plugin" % name,
        generates_api = True,
        processor_class = "com.squareup.moshi.kotlin.codegen.apt.JsonClassCodegenProcessor",
        deps = [moshi_compiler_dep, moshi_exported_library],
    )
    java_library(
        name = name,
        exported_plugins = [":%s_plugin" % name],
        visibility = visibility,
        exports = [moshi_exported_library],
    )
