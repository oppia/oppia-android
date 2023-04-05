"""
Provides Starlark macros for pinning downloaded Maven dependencies for local analysis & tracking.
"""

load("@maven_app//:defs.bzl", pinned_maven_install_app = "pinned_maven_install")
load("@maven_scripts//:defs.bzl", pinned_maven_install_scripts = "pinned_maven_install")

def pinMavenDependencies():
    """
    Pins Maven dependencies downloaded via maven_deps.bzl.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:maven_deps_pinner.bzl", "pinMavenDependencies")
        pinMavenDependencies()

    Note that this can only be called after Maven dependencies have been downloaded using
    maven_deps.bzl.
    """

    # TODO: Verify removing this fails build correctly.
    # TODO: Update third-party deps collection to include scripts.
    pinned_maven_install_app()
    pinned_maven_install_scripts()
