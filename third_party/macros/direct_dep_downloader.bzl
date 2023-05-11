"""
Provides Starlark macros for importing direct dependencies needed to build Oppia Android.
"""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file", "http_jar")
load(":direct_dep_defs.bzl", "IMPORT_TYPE", "PATCH_ORIGIN")

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
    _set_up_http_import(
        import_details,
        maven_repositories,
        import_dep_without_patches = http_archive,
        import_dep_with_patches = http_archive,
    )

def _set_up_http_jar_dependency(import_details, maven_repositories):
    _set_up_http_import(import_details, maven_repositories, import_dep_without_patches = http_jar)

def _set_up_http_file_dependency(import_details, maven_repositories):
    _set_up_http_import(import_details, maven_repositories, import_dep_without_patches = http_file)

def _set_up_http_import(
        import_details,
        maven_repositories,
        import_dep_without_patches,
        import_dep_with_patches = None):
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
    local_patches = _extract_local_patches(import_details["patches_details"])
    remote_patches = _extract_remote_patches(import_details["patches_details"])
    if len(local_patches) != 0 or len(remote_patches) != 0:
        if import_dep_with_patches == None:
            fail("This method of importing an HTTP dependency does not support patching.")
        import_dep_with_patches(
            name = import_details.get("import_bind_name") or import_details["name"],
            urls = unique_urls,
            sha256 = dependency_details["sha"],
            strip_prefix = (
                strip_prefix_template.format(version) if strip_prefix_template != None else None
            ),
            patches = local_patches,
            remote_patches = remote_patches,
            remote_patch_strip = import_details.get("remote_patch_path_start_removal_count"),
            workspace_file = import_details.get("workspace_file"),
        )
    elif import_dep_without_patches == http_file:
        import_dep_without_patches(
            name = import_details.get("import_bind_name") or import_details["name"],
            urls = unique_urls,
            sha256 = dependency_details["sha"],
            executable = dependency_details["executable"],
        )
    else:
        import_dep_without_patches(
            name = import_details.get("import_bind_name") or import_details["name"],
            urls = unique_urls,
            sha256 = dependency_details["sha"],
            strip_prefix = (
                strip_prefix_template.format(version) if strip_prefix_template != None else None
            ),
        )

def _set_up_git_repository_dependency(import_details):
    dependency_details = import_details["dependency_details"]
    local_patches = _extract_local_patches(import_details["patches_details"])
    remote_patches = _extract_remote_patches(import_details["patches_details"])
    if len(remote_patches) != 0:
        fail("Git repository imports do not currently support remote patches.")
    git_repository(
        name = import_details.get("import_bind_name") or import_details["name"],
        commit = dependency_details["commit"],
        remote = dependency_details["remote"],
        repo_mapping = dependency_details.get("repo_mapping") or {},
        build_file = dependency_details["build_file"],
        patches = local_patches,
    )

def _set_up_remote_dependency(import_details, maven_repositories):
    if import_details["import_type"] == IMPORT_TYPE.HTTP_ARCHIVE:
        _set_up_http_archive_dependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.HTTP_JAR:
        _set_up_http_jar_dependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.HTTP_FILE:
        _set_up_http_file_dependency(import_details, maven_repositories)
    elif import_details["import_type"] == IMPORT_TYPE.GIT_REPOSITORY:
        _set_up_git_repository_dependency(import_details)
    else:
        fail("Unsupported import type: %s." % import_details["import_type"])

def _extract_local_patches(patches_details):
    return [
        patch_details["file"]
        for patch_details in patches_details
        if patch_details["origin"] == PATCH_ORIGIN.LOCAL
    ]

def _extract_remote_patches(patches_details):
    return {
        patch_details["url"]: patch_details["sri"]
        for patch_details in patches_details
        if patch_details["origin"] == PATCH_ORIGIN.REMOTE
    }
