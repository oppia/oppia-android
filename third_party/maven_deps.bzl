"""
Provides Starlark macros for importing Maven-hosted dependencies.
"""

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load("//third_party:versions.bzl", "MAVEN_ARTIFACT_TREES", "install_maven_dependencies")

def downloadMavenDependencies():
    """
    Loads the Maven-hosted dependencies needed to be able to build the Oppia Android project.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:maven_deps.bzl", "downloadMavenDependencies")
        downloadMavenDependencies()

    Note that this can only be called after toolchains are set up using tools/toolchains.bzl as it
    requires supporting rules_jvm_external.
    """

    # Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external. Note to
    # developers: new dependencies should be added to third_party/versions.bzl, not here.
    [
        install_maven_dependencies(maven, maven_install, parse, build_context)
        for build_context in MAVEN_ARTIFACT_TREES.keys()
    ]
