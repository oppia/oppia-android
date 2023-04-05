"""
Provides Starlark macros for importing Maven-hosted dependencies.
"""

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load("@dagger//:workspace_defs.bzl", "DAGGER_ARTIFACTS", "DAGGER_REPOSITORIES")
load("//third_party:versions.bzl", "MAVEN_ARTIFACT_TREES", "MAVEN_REPOSITORIES", "extract_maven_dependencies")

def downloadMavenDependencies():
    """
    Loads the Maven-hosted dependencies needed to be able to build the Oppia Android project.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:maven_deps.bzl", "downloadMavenDependencies")
        downloadMavenDependencies()

    Note that this can only be called after toolchains are set up using tools/toolchains.bzl as it
    requires supporting rules_jvm_external.
    """

    # Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external. Note to developers:
    # new dependencies should be added to third_party/versions.bzl, not here.
    [
        maven_install(
            name = "maven_%s" % build_context,
            artifacts = extract_maven_dependencies(maven, parse, artifact_tree, DAGGER_ARTIFACTS),
            duplicate_version_warning = "error",
            fail_if_repin_required = True,
            maven_install_json = "//third_party:maven_install_%s.json" % build_context,
            override_targets = artifact_tree["target_overrides"],
            repositories = DAGGER_REPOSITORIES + artifact_tree["repositories"],
            strict_visibility = True,
        )
        for build_context, artifact_tree in MAVEN_ARTIFACT_TREES.items()
    ]
