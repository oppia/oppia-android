"""
Provides Starlark macros for importing direct dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load(":load_defs.bzl", "IMPORT_TYPE")

def _setUpHttpArchiveDependency(import_details, maven_repositories):
    _setUpHttpImport(import_details, maven_repositories, http_archive)

def _setUpHttpJarDependency(import_details, maven_repositories):
    _setUpHttpImport(import_details, maven_repositories, http_jar)

def _setUpHttpImport(import_details, maven_repositories, import_dep):
    dependency_details = import_details["dependency_details"]
    version = dependency_details["version"]
    strip_prefix_template = dependency_details.get("strip_prefix_template")
    url_templates = dependency_details["url_templates"]
    expanded_urls = [
        url_template.format(version, maven_repository)
        for url_template in url_templates
        for maven_repository in maven_repositories
    ]
    unique_urls = []
    for expanded_url in expanded_urls:
        if expanded_url not in unique_urls:
            unique_urls.append(expanded_url)
    import_dep(
        name = import_details.get("import_bind_name") or import_details["name"],
        urls = unique_urls,
        sha256 = dependency_details["sha"],
        strip_prefix = (
            strip_prefix_template.format(version) if strip_prefix_template != None else None
        ),
    )

def _setUpGitRepositoryDependency(import_details):
    dependency_details = import_details["dependency_details"]
    git_repository(
        name = import_details.get("import_bind_name") or import_details["name"],
        commit = dependency_details["commit"],
        remote = dependency_details["remote"],
        shallow_since = dependency_details["shallow_since"],
        repo_mapping = dependency_details.get("repo_mapping") or {},
    )

def _setUpRemoteDependency(import_details, maven_repositories):
    if import_details["import_type"] == IMPORT_TYPE.HTTP_ARCHIVE:
        _setUpHttpArchiveDependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.HTTP_JAR:
        _setUpHttpJarDependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.GIT_REPOSITORY:
        _setUpGitRepositoryDependency(import_details)
    else:
        fail("Unsupported import type: %s." % import_details["import_type"])

# TODO: Fix all camel casing in Bazel files.
def downloadDirectWorkspaceDependencies(dependencies, maven_repositories):
    """
    Loads the direct Bazel workspace dependencies needed to be able to build the Oppia Android
    project. This may not contain all build-time dependencies as many of those are provided by Maven
    via maven_deps.bzl.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:direct_deps.bzl", "downloadDirectWorkspaceDependencies")
        downloadDirectWorkspaceDependencies()

    Note also that toolchains may need to be set up after loading this dependencies (see
    third_party/tools/toolchains.bzl).
    """

    # Set up all dependencies (the order doesn't matter here since it's just downloading
    # corresponding HTTP archives/jars or cloning git repositories without building them).
    for import_details in dependencies:
        _setUpRemoteDependency(import_details, maven_repositories)
