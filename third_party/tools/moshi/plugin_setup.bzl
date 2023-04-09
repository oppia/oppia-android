"""
Provides a macro for setting up support for using Moshi in Java target builds.
"""

load("@rules_java//java:defs.bzl", "java_library", "java_plugin")

def set_up(name, visibility, moshi_compiler_dep, moshi_exported_library):
    """Defines a new java_library that exposes Moshi as both a dependency and annotation processor.
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
