"""
Provides a macro for setting up support for using Glide in Java target builds.
"""

load("@rules_java//java:defs.bzl", "java_library", "java_plugin")

def set_up(name, visibility, glide_compiler_dep):
    """Defines a new java_library that exposes Glide as both a dependency and annotation processor.
    """
    java_plugin(
        name = "%s_plugin" % name,
        generates_api = True,
        processor_class = "com.bumptech.glide.annotation.compiler.GlideAnnotationProcessor",
        deps = [glide_compiler_dep],
    )

    # Define a separate target for the Glide annotation processor compiler. Unfortunately, this library
    # can't encapsulate all of Glide (i.e. by exporting the main Glide dependency) since that includes
    # Android assets which java_library targets do not export.
    java_library(
        name = name,
        exported_plugins = [":%s_plugin" % name],
        visibility = visibility,
        exports = [glide_compiler_dep],
    )
