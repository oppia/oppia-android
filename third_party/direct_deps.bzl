"""
Provides Starlark macros for importing direct dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load(":versions.bzl", "GIT_REPOSITORY_DEPENDENCY_VERSIONS", "HTTP_ARCHIVE_DEPENDENCY_VERSIONS", "HTTP_JAR_DEPENDENCY_VERSIONS")

def _setUpHttpArchiveDependency(name):
    metadata = HTTP_ARCHIVE_DEPENDENCY_VERSIONS[name]
    strip_prefix = metadata.get("strip_prefix")
    version = metadata["version"]

    http_archive(
        name = metadata.get("import_bind_name") or name,
        urls = [url.format(version) for url in metadata["urls"]],
        sha256 = metadata["sha"],
        strip_prefix = strip_prefix.format(version) if not (strip_prefix == None) else None,
    )

def _setUpHttpJarDependency(name):
    metadata = HTTP_JAR_DEPENDENCY_VERSIONS[name]
    strip_prefix = metadata.get("strip_prefix")
    version = metadata["version"]

    http_jar(
        name = metadata.get("import_bind_name") or name,
        urls = [url.format(version) for url in metadata["urls"]],
        sha256 = metadata["sha"],
        strip_prefix = strip_prefix.format(version) if not (strip_prefix == None) else None,
    )

def _setUpGitRepositoryDependency(name):
    metadata = GIT_REPOSITORY_DEPENDENCY_VERSIONS[name]

    git_repository(
        name = metadata.get("import_bind_name") or name,
        commit = metadata["commit"],
        remote = metadata["remote"],
        shallow_since = metadata["shallow_since"],
        repo_mapping = metadata.get("repo_mapping") or {},
    )

def downloadDirectWorkspaceDependencies():
    """
    Loads the direct Bazel workspace dependencies needed to be able to build the Oppia Android
    project. This may not contain all build-time dependencies as many of those are provided by Maven
    via maven_deps.bzl.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:direct_deps.bzl", "downloadDirectWorkspaceDependencies")
        downloadDirectWorkspaceDependencies()

    Note also that toolchains may need to be set up after loading this dependencies (see
    tools/toolchains.bzl).
    """

    # Set up all dependencies (the order doesn't matter here since it's just downloading
    # corresponding HTTP archives/jars or cloning git repositories without building them).
    for dependency_name in HTTP_ARCHIVE_DEPENDENCY_VERSIONS.keys():
        _setUpHttpArchiveDependency(name = dependency_name)
    for dependency_name in HTTP_JAR_DEPENDENCY_VERSIONS.keys():
        _setUpHttpJarDependency(name = dependency_name)
    for dependency_name in GIT_REPOSITORY_DEPENDENCY_VERSIONS.keys():
        _setUpGitRepositoryDependency(name = dependency_name)
