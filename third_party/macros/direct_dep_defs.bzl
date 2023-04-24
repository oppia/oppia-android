"""
Macros for defining external dependency references through multiple mechanisms. The structures
created by these definitions are used by //third_party/macros:direct_dep_downloader.bzl and
//third_party/macros:direct_dep_loader.bzl.
"""

# Represents how an imported dependency should be made available to other libraries.
EXPORT_TYPE = struct(
    # Indicates a dependency should be exported as a library.
    LIBRARY = struct(export_type_enum_value = 0),

    # Indicates a dependency should be exported as a Bazel-runnable binary.
    BINARY = struct(export_type_enum_value = 1),
)

# Represents the required toolchain for exporting an imported dependency.
EXPORT_TOOLCHAIN = struct(
    # Indicates a dependency requires the Android toolchain.
    ANDROID = struct(export_toolchain_enum_value = 0),

    # Indicates a dependency requires the JVM toolchain.
    JAVA = struct(export_toolchain_enum_value = 1),

    # Indicates a dependency requires the Kotlin toolchain.
    KOTLIN = struct(export_toolchain_enum_value = 2),
)

# Represents the type of import approach required for a dependency.
IMPORT_TYPE = struct(
    # Indicates that the dependency is an HTTP archive, per Bazel's http_archive.
    HTTP_ARCHIVE = struct(import_type_enum_value = 0),

    # Indicates that the dependency is an HTTP Jar file, per Bazel's http_jar.
    HTTP_JAR = struct(import_type_enum_value = 1),

    # Indicates that the dependency is a git repository, per Bazel's git_repository.
    GIT_REPOSITORY = struct(import_type_enum_value = 2),
)

# Represents a type of patch to apply to an imported dependency.
PATCH_ORIGIN = struct(
    # Indicates a patch that is included within the local repository.
    LOCAL = struct(patch_origin_enum_value = 0),

    # Indicates a patch that is remotely available to download.
    REMOTE = struct(patch_origin_enum_value = 1),
)

def create_http_archive_reference(
        name,
        sha,
        version,
        test_only,
        import_bind_name = None,
        url = None,
        urls = None,
        maven_url_suffix = None,
        strip_prefix_template = None,
        patches_details = [],
        remote_patch_path_start_removal_count = 0,
        workspace_file = None,  # TODO: Document.
        export_details = None,
        exports_details = None):
    """Creates and returns a structure that will be imported using Bazel's http_archive.

    Args:
        name: str. The name of the archive that will be used to reference it as an external
            workspace. That is, the archive's contents will be made available using "@<name>//...".
        sha: str. The SHA-256 hash used to validate the archive when it's downloaded.
        version: str. The version of the archive to be downloaded.
        test_only: boolean. Whether this archive should be made available only to test targets.
        import_bind_name: str. The name to bind the archive to, instead of 'name', when referencing
            its contents, or None if 'name' should be used, instead. The default value is None.
        url: str. The URL template from which to download the archive. This should use "{0}" for
            places where the archive's version should be interpolated. Exactly one of this, urls, or
            maven_url_prefix must be provided (but not more than one).
        urls: list of str. The list of URL templates from which to download the archive, following
            the same constraints as 'url'. Bazel will use extra URLs as mirrors in case the archive
            is temporarily unavailable from any of the others.
        maven_url_suffix: str. A URL template with the same constraints as 'url' except the
            beginning of the URL is omitted since it will be replaced with a link to one of the
            Maven repositories that the build system depends on. All Maven repositories will be used
            by default for redundancy.
        strip_prefix_template: str. An optional prefix to strip from the directory path imported
            from the archive with optional templating (in the same way as 'url': "{0}" will be
            replaced with the archive's version). See http_archive's 'strip_prefix' for more.
        patches_details: list of dicts. An optional list of patch configurations to apply to the
            imported dependency. Each dict is expected to have been created by one of the
            create_*_patch_config functions. This defaults to an empty list.
        remote_patch_path_start_removal_count: int. An optional integer specifying the number of
            leading path fragments to remove from remote patches before applying them. This defaults
            to 0, but oftentimes '1' is required as it removes the normal "a/" and "b/" prefixes.
        export_details: dict. An optional dict created using one of create_export_library_details or
            create_export_binary_details. When provided, the system will ensure this archive is made
            accessible under a "third_party:<alias>" reference (where the alias is defined as part
            of the export details).
        exports_details: list of dicts. An optional list of export details that are each constrained
            by the same specifics as 'export_details' above.
    """
    return _create_http_import_reference(
        name,
        import_type = IMPORT_TYPE.HTTP_ARCHIVE,
        sha = sha,
        version = version,
        test_only = test_only,
        import_bind_name = import_bind_name,
        url = url,
        urls = urls,
        maven_url_suffix = maven_url_suffix,
        strip_prefix_template = strip_prefix_template,
        patches_details = patches_details,
        remote_patch_path_start_removal_count = remote_patch_path_start_removal_count,
        workspace_file = workspace_file,
        export_details = export_details,
        exports_details = exports_details,
    )

def create_http_jar_reference(
        name,
        sha,
        version,
        test_only,
        import_bind_name = None,
        url = None,
        urls = None,
        maven_url_suffix = None,
        strip_prefix_template = None,
        export_details = None,
        exports_details = None):
    """Creates and returns a structure that will be imported using Bazel's http_jar.

    Args:
        name: str. The name of the archive that will be used to reference it as an external
            workspace. See create_http_archive_reference for more specifics.
        sha: str. The SHA-256 hash used to validate the archive when it's downloaded.
        version: str. The version of the archive to be downloaded.
        test_only: boolean. Whether this archive should be made available only to test targets.
        import_bind_name: str. The name to bind the archive to. See create_http_archive_reference
            for more specifics.
        url: str. The URL template from which to download the archive. See
            create_http_archive_reference for more specifics.
        urls: list of str. The list of URL templates from which to download the archive. See
            create_http_archive_reference for more specifics.
        maven_url_suffix: str. A Maven-populated URL from which to download the archive. See
            create_http_archive_reference for more specifics.
        strip_prefix_template: str. An optional prefix to strip from the archive's directory path.
            See create_http_archive_reference for more specifics.
        export_details: dict. An optional dict specifying how to export this library. See
            create_http_archive_reference for more specifics.
        exports_details: list of dicts. An optional list of export details. See
            create_http_archive_reference for more specifics.
    """
    return _create_http_import_reference(
        name,
        import_type = IMPORT_TYPE.HTTP_JAR,
        sha = sha,
        version = version,
        test_only = test_only,
        import_bind_name = import_bind_name,
        url = url,
        urls = urls,
        maven_url_suffix = maven_url_suffix,
        strip_prefix_template = strip_prefix_template,
        patches_details = [],
        remote_patch_path_start_removal_count = None,
        workspace_file = None,
        export_details = export_details,
        exports_details = exports_details,
    )

def create_git_repository_reference(
        name,
        commit,
        remote,
        test_only,
        import_bind_name = None,
        repo_mapping = {},
        build_file = None,
        patches_details = [],
        export_details = None,
        exports_details = None):
    """Creates and returns a structure that will be imported using Bazel's git_repository.

    The remote repository must be a Bazel-compatible repository, or build_file needs to be provided.

    Args:
        name: str. The name of the archive that will be used to reference it as an external
            workspace. See create_http_archive_reference for more specifics.
        commit: str. The SHA-1 commit hash that should be checked out.
        remote: str. The remote URL of the Git repository.
        test_only: boolean. Whether this archive should be made available only to test targets.
        import_bind_name: str. The name to bind the archive to. See create_http_archive_reference
            for more specifics.
        repo_mapping: dict. A string-to-string dictionary representing how certain workspaces should
            be aliased in the imported workspace. For example, a repo_mapping of
            '{"@maven": "@maven_app"}' will instruct Bazel to treat all references to "@maven" as
            "@maven_app", instead, when building the imported workspace, giving more control to the
            main workspace to configure its imported dependencies.
        build_file: str. The target label for the build file to use when building the remote
            repository. This is None by default where None indicates to not create a custom build
            file.
        patches_details: list of dicts. An optional list of patch configurations See
            create_http_archive_reference for more specifics.
        export_details: dict. An optional dict specifying how to export this library. See
            create_http_archive_reference for more specifics.
        exports_details: list of dicts. An optional list of export details. See
            create_http_archive_reference for more specifics.
    """
    return _create_import_dependency_reference(
        name = name,
        import_type = IMPORT_TYPE.GIT_REPOSITORY,
        test_only = test_only,
        dependency_details = {
            "build_file": build_file,
            "commit": commit,
            "remote": remote,
            "repo_mapping": repo_mapping,
        },
        import_bind_name = import_bind_name,
        patches_details = patches_details,
        export_details = export_details,
        exports_details = exports_details,
    )

def create_export_library_details(
        exposed_artifact_name,
        exportable_target,
        export_toolchain,
        should_be_visible_to_maven_targets = False,
        runtime_deps = []):
    """Returns a structure that specifies how a dependency should be made available as a library.

    Note that this does not actually create a wrapper library, only the instructions for creating
    one. create_direct_import_dependency_wrappers is ultimately responsible for creating the
    wrappers and will control visibilities and full target names.

    Args:
        exposed_artifact_name: str. The name of the exported library under the immediate Bazel
            package (usually a 'third_party' directory).
        exportable_target: str. The relative target within the imported archive that can be exposed.
            This is often "jar" for imported Jar files, or a specific build target for repositories.
        export_toolchain: EXPORT_TOOLCHAIN. The specific toolchain that should be used when creating
            the exported wrapper library.
        should_be_visible_to_maven_targets: boolean. Whether the exported library should be made
            specially available to local internal Maven dependencies. This defaults to False and is
            generally only needed for Maven dependency overrides.
        runtime_deps: list of str. Additional targets to automatically depend on, at runtime, when
            using the wrapper library. This defaults to an empty list and is only needed in advanced
            cases.
    """
    return {
        "runtime_deps": runtime_deps,
        "export_toolchain": export_toolchain,
        "export_type": EXPORT_TYPE.LIBRARY,
        "exportable_target": exportable_target,
        "exposed_artifact_name": exposed_artifact_name,
        "should_be_visible_to_maven_targets": should_be_visible_to_maven_targets,
    }

def create_export_binary_details(
        exposed_artifact_name,
        main_class,
        exportable_runtime_target,
        runtime_deps = []):
    """Returns a structure that specifies how a dependency should be made available as a binary.

    Note that this has the same limitations and restrictions as create_export_library_details.

    Note also that binaries always use the Java toolchain.

    Args:
        exposed_artifact_name: str. The name of the exported library under the immediate Bazel
            package (usually a 'third_party' directory).
        main_class: str. The fully-qualified Java class containing the binary's main() function.
        exportable_runtime_target: str. The relative target within the imported archive that can be
            exposed for execution. This is often "jar" for imported Jar files, or a specific build
            target for repositories.
        runtime_deps: list of str. Additional targets to automatically depend on, at runtime, when
            using the wrapper binary. This defaults to an empty list and is only needed in advanced
            cases.
    """
    return {
        "runtime_deps": runtime_deps,
        "export_toolchain": EXPORT_TOOLCHAIN.JAVA,
        "export_type": EXPORT_TYPE.BINARY,
        "exportable_runtime_target": exportable_runtime_target,
        "exposed_artifact_name": exposed_artifact_name,
        "main_class": main_class,
    }

def create_maven_artifact_configuration(
        production_dep_config,
        test_dep_config,
        maven_install_json_target,
        target_overrides = {}):
    """Returns a structure that specifies the complete definition of a new imported Maven workspace.

    Args:
        production_dep_config: dict. The configuration, as created by create_dep_config() below, of
            production dependencies to download. Note that these dependencies will be available to
            both test & non-test targets.
        test_dep_config: dict. The configuration, as created by create_dep_config() below, of test
            dependencies to download. Note that these dependencies will only be available to test
            targets.
        maven_install_json_target: str. The absolute file target to which a Maven installation
            manifest will be referenced (or created).
        target_overrides: dict. An optional dictionary of string-to-string that forcibly overrides
            Maven dependencies to local targets, instead. This is only used for advanced cases and
            can have significant stability implications if not used carefully. The default value is
            an empty dict (indicating no overrides).
    """
    return {
        "deps": {
            "prod": production_dep_config,
            "test": test_dep_config,
        },
        "maven_install_json": maven_install_json_target,
        "target_overrides": target_overrides,
    }

def create_dep_config(direct_deps, transitive_deps, exclusions = None):
    """Returns a structure that specifies versions of dependencies to download.

    Args:
        direct_deps: dict. A dictionary of Maven coordinate strings to version strings that should
            be made directly available (e.g. via wrappers created by direct_dep_loader.bzl).
        transitive_deps: dict. A dictionary of Maven coordinate strings to version strings that are
            indirectly required for the direct_deps to be available. These are usually supplied by a
            list automatically generated via an up-to-date installation manifest file and
            //scripts:validate_maven_dependencies.
        exclusions: list of str. An optional list of non-version Maven coordinate strings that
            should never be included when downloading the dependencies provided by this
            configuration. This is an advanced option that turns a compile-time guarantee into
            potential runtime errors--it should only be used when excluded dependencies can be
            guaranteed to never be needed at runtime. This defaults to None (indicating no
            exclusions).
    """
    return {
        "direct": direct_deps,
        "exclusions": exclusions,
        "transitive": transitive_deps,
    }

def create_local_patch_config(patch_file):
    """Returns a structure that specifies a local patch to apply to an imported dependency.

    Args:
        patch_file: str. The Bazel file target label corresponding to a local patch file to apply.
    """
    return {
        "file": patch_file,
        "origin": PATCH_ORIGIN.LOCAL,
    }

def create_remote_patch_config(patch_url, patch_sri):
    """Returns a structure that specifies a remote patch to apply to an imported dependency.

        Args:
            patch_url: str. The URL to the patch to download.
            patch_sri: str. The Subresource Integrity (SRI) of the patch. See
                https://www.srihash.org/ and
                https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity for more
                context and instructions on how to generate a hash. Alternatively, you can use this
                command via a terminal:
                ```sh
                echo "sha256-$(wget -q -O- <patch_url> | openssl dgst -sha256 -binary | openssl base64 -A)"
                ```
                and copy the result to use as the patch's SRI.
    """
    return {
        "origin": PATCH_ORIGIN.REMOTE,
        "sri": patch_sri,
        "url": patch_url,
    }

def _create_http_import_reference(
        name,
        import_type,
        sha,
        version,
        test_only,
        import_bind_name,
        url,
        urls,
        maven_url_suffix,
        strip_prefix_template,
        patches_details,
        remote_patch_path_start_removal_count,
        workspace_file,
        export_details,
        exports_details):
    # The '+' here is working around syntax issues when trying to use XOR.
    if int(url != None) + int(urls != None) + int(maven_url_suffix != None) != 1:
        fail("Expected exactly one of url, urls, or maven_template_url to be defined.")
    if url != None:
        urls = [url]
    elif maven_url_suffix != None:
        urls = ["{1}/%s" % maven_url_suffix]
    return _create_import_dependency_reference(
        name = name,
        import_type = import_type,
        test_only = test_only,
        dependency_details = {
            "sha": sha,
            "strip_prefix_template": strip_prefix_template,
            "url_templates": urls,
            "version": version,
        },
        import_bind_name = import_bind_name,
        patches_details = patches_details,
        remote_patch_path_start_removal_count = remote_patch_path_start_removal_count,
        workspace_file = workspace_file,
        export_details = export_details,
        exports_details = exports_details,
    )

def _create_import_dependency_reference(
        name,
        import_type,
        test_only,
        dependency_details,
        import_bind_name,
        patches_details,
        export_details = None,
        exports_details = None,
        remote_patch_path_start_removal_count = None,
        workspace_file = None):
    if export_details != None and exports_details != None:
        fail("Expected exactly one of export_details or exports_details to be defined.")
    if export_details != None:
        exports_details = [export_details]
    return {
        "dependency_details": dependency_details,
        "exports_details": exports_details or [],
        "import_bind_name": import_bind_name,
        "import_type": import_type,
        "name": name,
        "remote_patch_path_start_removal_count": remote_patch_path_start_removal_count,
        "workspace_file": workspace_file,
        "patches_details": patches_details,
        "test_only": test_only,
    }
