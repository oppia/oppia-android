"""
This file initializes all external dependencies & toolchains needed to build Oppia Android.
"""

# Note to developer: The order of loads & calls below MUST NOT BE CHANGED--it will guarantee a
# breakage as subsequent loads depend on both previous loads & calls. There is no other order
# possible for the configuration below, and it ought to never need to be changed.

workspace(name = "oppia_android")

# The list of repositories from which all Maven-tied dependencies may be downloaded.
_MAVEN_REPOSITORIES = [
    "https://maven.fabric.io/public",
    "https://maven.google.com",
    "https://repo1.maven.org/maven2",
]

load("//third_party:direct_deps.bzl", "downloadDirectWorkspaceDependencies")
load("//third_party:versions.bzl", app_remote_deps = "DIRECT_REMOTE_DEPENDENCIES")
load("//scripts/third_party:versions.bzl", scripts_remote_deps = "DIRECT_REMOTE_DEPENDENCIES")

downloadDirectWorkspaceDependencies(app_remote_deps, _MAVEN_REPOSITORIES)

downloadDirectWorkspaceDependencies(scripts_remote_deps, _MAVEN_REPOSITORIES)

load("//third_party/tools:toolchains.bzl", "initializeToolchainsForWorkspace")

initializeToolchainsForWorkspace()

load("//third_party:load_utilities.bzl", "downloadMavenDependencies")
load("//third_party:versions.bzl", app_maven_artifact_tree = "MAVEN_ARTIFACT_TREES")
load("//scripts/third_party:versions.bzl", scripts_maven_artifact_tree = "MAVEN_ARTIFACT_TREES")

downloadMavenDependencies(
    maven_artifact_tree = app_maven_artifact_tree,
    maven_repositories = _MAVEN_REPOSITORIES,
    maven_repository_name = "maven_app",
)

downloadMavenDependencies(
    maven_artifact_tree = scripts_maven_artifact_tree,
    maven_repositories = _MAVEN_REPOSITORIES,
    maven_repository_name = "maven_scripts",
)

load("//third_party:maven_deps_pinner.bzl", "pinMavenDependencies")

pinMavenDependencies()
