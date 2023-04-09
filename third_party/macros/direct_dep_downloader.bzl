"""
Provides Starlark macros for importing direct dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_jar")
load(":direct_dep_defs.bzl", "IMPORT_TYPE")

def download_direct_workspace_dependencies(dependencies, maven_repositories):
    """Loads the direct Bazel workspace dependencies needed to be able to build the project.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party/macros:direct_dep_downloader.bzl", "download_direct_workspace_dependencies")
        download_direct_workspace_dependencies()

    Note also that toolchains may need to be set up after loading this dependencies (see
    //third_party/tools:toolchains.bzl).

    Args:
        dependencies: list of dict. The list of dependencies where is dependency is created using
            one of the create_*_reference() macros from direct_dep_defs.bzl. This may not contain
            all build-time dependencies as many of those are provided by Maven via
            direct_dep_loader.bzl.
        maven_repositories: list of str. The list of Maven repository URLs that should be used when
            downloading direct dependencies which are pulled from Maven.
    """

    # Set up all dependencies (the order doesn't matter here since it's just downloading
    # corresponding HTTP archives/jars or cloning git repositories without building them).
    for import_details in dependencies:
        _set_up_remote_dependency(import_details, maven_repositories)

def _set_up_http_archive_dependency(import_details, maven_repositories):
    _set_up_http_import(import_details, maven_repositories, http_archive)

def _set_up_http_jar_dependency(import_details, maven_repositories):
    _set_up_http_import(import_details, maven_repositories, http_jar)

def _set_up_http_import(import_details, maven_repositories, import_dep):
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

def _set_up_git_repository_dependency(import_details):
    dependency_details = import_details["dependency_details"]
    git_repository(
        name = import_details.get("import_bind_name") or import_details["name"],
        commit = dependency_details["commit"],
        remote = dependency_details["remote"],
        shallow_since = dependency_details["shallow_since"],
        repo_mapping = dependency_details.get("repo_mapping") or {},
    )

def _set_up_remote_dependency(import_details, maven_repositories):
    if import_details["import_type"] == IMPORT_TYPE.HTTP_ARCHIVE:
        _set_up_http_archive_dependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.HTTP_JAR:
        _set_up_http_jar_dependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.GIT_REPOSITORY:
        _set_up_git_repository_dependency(import_details)
    else:
        fail("Unsupported import type: %s." % import_details["import_type"])
