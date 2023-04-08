# TODO: Add docs for this func, other functions, and the file.

EXPORT_TYPE = struct(
    LIBRARY = struct(export_type_enum_value = 0),
    BINARY = struct(export_type_enum_value = 1),
)

EXPORT_TOOLCHAIN = struct(
    ANDROID = struct(export_toolchain_enum_value = 0),
    JAVA = struct(export_toolchain_enum_value = 1),
    KOTLIN = struct(export_toolchain_enum_value = 2),
)

IMPORT_TYPE = struct(
    HTTP_ARCHIVE = struct(import_type_enum_value = 0),
    HTTP_JAR = struct(import_type_enum_value = 1),
    GIT_REPOSITORY = struct(import_type_enum_value = 2),
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
        export_details = None,
        exports_details = None):
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
        export_details = export_details,
        exports_details = exports_details,
    )

def create_git_repository_reference(
        name,
        commit,
        remote,
        shallow_since,
        test_only,
        import_bind_name = None,
        repo_mapping = {},
        export_details = None,
        exports_details = None):
    return _create_import_dependency_reference(
        name = name,
        import_type = IMPORT_TYPE.GIT_REPOSITORY,
        test_only = test_only,
        dependency_details = {
            "commit": commit,
            "remote": remote,
            "repo_mapping": repo_mapping,
            "shallow_since": shallow_since,
        },
        import_bind_name = import_bind_name,
        export_details = export_details,
        exports_details = exports_details,
    )

def create_export_library_details(
        exposed_artifact_name,
        exportable_target,
        export_toolchain,
        should_be_visible_to_maven_targets = False,
        additional_exports = []):
    return {
        "export_type": EXPORT_TYPE.LIBRARY,
        "export_toolchain": export_toolchain,
        "exportable_target": exportable_target,
        "exposed_artifact_name": exposed_artifact_name,
        "should_be_visible_to_maven_targets": should_be_visible_to_maven_targets,
        "additional_exports": additional_exports,
    }

def create_export_binary_details(
        exposed_artifact_name,
        main_class,
        exportable_runtime_target,
        additional_exports = []):
    return {
        "export_type": EXPORT_TYPE.BINARY,
        "export_toolchain": EXPORT_TOOLCHAIN.JAVA,
        "exportable_runtime_target": exportable_runtime_target,
        "exposed_artifact_name": exposed_artifact_name,
        "main_class": main_class,
        "additional_exports": additional_exports,
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
            "version": version,
            "url_templates": urls,
            "strip_prefix_template": strip_prefix_template,
        },
        import_bind_name = import_bind_name,
        export_details = export_details,
        exports_details = exports_details,
    )

def _create_import_dependency_reference(
        name,
        import_type,
        test_only,
        dependency_details,
        import_bind_name = None,
        export_details = None,
        exports_details = None):
    if export_details != None and exports_details != None:
        fail("Expected exactly one of export_details or exports_details to be defined.")
    if export_details != None:
        exports_details = [export_details]
    return {
        "name": name,
        "import_type": import_type,
        "test_only": test_only,
        "dependency_details": dependency_details,
        "import_bind_name": import_bind_name,
        "exports_details": exports_details or [],
    }
