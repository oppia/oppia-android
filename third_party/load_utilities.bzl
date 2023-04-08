"""
Provides Starlark macros for importing and wrapping both Maven-hosted and directly referenced remote
dependencies.
"""

load("@rules_jvm_external//:defs.bzl", "artifact", "maven_install")
load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_import", "kt_jvm_library")
load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load(":load_defs.bzl", "EXPORT_TOOLCHAIN", "EXPORT_TYPE")

# TODO: Add docs for this func, other functions, and the file (needs update).

# TODO: Convert to docstring.
# Create android library wrappers for select dependencies. Note that artifact is used so that the
# correct Maven repository is selected. Also, dependencies are restricted to specific visibility
# scopes (e.g. app vs. scripts), or to specific build contexts (e.g. tests) to help ensure
# cross-dependencies can't accidentally occur.

def create_maven_dependency_wrappers(
        maven_repository_name,
        direct_prod_dependencies,
        direct_test_dependencies,
        prod_artifact_visibility,
        test_artifact_visibility):
    _wrap_maven_dependencies(
        maven_repository_name,
        direct_prod_dependencies,
        prod_artifact_visibility,
        test_only = False,
    )
    _wrap_maven_dependencies(
        maven_repository_name,
        direct_test_dependencies,
        test_artifact_visibility,
        test_only = True,
    )

def create_direct_import_dependency_wrappers(
        dependency_imports_details,
        prod_artifact_visibility,
        test_artifact_visibility,
        maven_artifact_visibility):
    for import_details in dependency_imports_details:
        for export_details in import_details["exports_details"]:
            _wrap_dependency(
                import_details,
                export_details,
                prod_artifact_visibility,
                test_artifact_visibility,
                maven_artifact_visibility,
            )

def downloadMavenDependencies(maven_repository_name, maven_artifact_tree, maven_repositories):
    """
    Loads the Maven-hosted dependencies needed to be able to build the Oppia Android project.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party:maven_deps.bzl", "downloadMavenDependencies")
        downloadMavenDependencies()

    Note that this can only be called after toolchains are set up using
    third_party/tools/toolchains.bzl as it requires supporting rules_jvm_external.
    """

    # Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external. Note to
    # developers: new dependencies should be added to third_party/versions.bzl, not here.
    maven_install(
        name = maven_repository_name,
        artifacts = _extract_maven_dependencies(maven_artifact_tree),
        duplicate_version_warning = "error",
        fail_if_repin_required = True,
        maven_install_json = maven_artifact_tree["maven_install_json"],
        override_targets = maven_artifact_tree.get("target_overrides") or {},
        repositories = maven_repositories,
        strict_visibility = True,
    )

def _wrap_maven_dependencies(
        maven_repository_name,
        dependency_versions,
        artifact_visibility,
        test_only):
    for name, version in dependency_versions.items():
        native.android_library(
            name = name.replace(":", "_").replace(".", "_"),
            testonly = test_only,
            visibility = artifact_visibility,
            exports = [artifact(
                "%s:%s" % (name, version),
                repository_name = maven_repository_name,
            )],
        )

def _wrap_dependency(
        import_details,
        export_details,
        prod_artifact_visibility,
        test_artifact_visibility,
        maven_artifact_visibility):
    name = import_details.get("import_bind_name") or import_details["name"]
    is_test_only = import_details["test_only"]
    base_visibility = test_artifact_visibility if is_test_only else prod_artifact_visibility

    if export_details["export_type"] == EXPORT_TYPE.LIBRARY:
        if export_details["export_toolchain"] == EXPORT_TOOLCHAIN.ANDROID:
            create_lib = native.android_library
            explicit_exports = ["@%s//%s" % (name, export_details["exportable_target"])]
        elif export_details["export_toolchain"] == EXPORT_TOOLCHAIN.JAVA:
            create_lib = java_library
            explicit_exports = ["@%s//%s" % (name, export_details["exportable_target"])]
        elif export_details["export_toolchain"] == EXPORT_TOOLCHAIN.KOTLIN:
            # A kt_jvm_import is needed to ensure that Kotlin metadata is included. However, the
            # output from kt_jvm_import needs to be wrapped in a kt_jvm_library in order to work
            # with the Bazel IntelliJ plugin correctly (since kt_jvm_library provides the expected
            # IDE output for syncing).
            create_lib = kt_jvm_library
            kt_jvm_import(
                name = "_%s_do_not_depend" % export_details["exposed_artifact_name"],
                jars = ["@%s//%s:file" % (name, export_details["exportable_target"])],
                tags = ["no-ide"],
                testonly = is_test_only,
            )
            explicit_exports = ["_%s_do_not_depend" % export_details["exposed_artifact_name"]]
        else:
            fail("Unsupported export type: %s." % export_details["export_toolchain"])
        should_be_visible_to_maven_targets = export_details["should_be_visible_to_maven_targets"]
        maven_visibility = maven_artifact_visibility if should_be_visible_to_maven_targets else []
        create_lib(
            name = export_details["exposed_artifact_name"],
            visibility = base_visibility + maven_visibility,
            exports = explicit_exports + export_details["additional_exports"],
            testonly = is_test_only,
        )
    elif export_details["export_type"] == EXPORT_TYPE.BINARY:
        if export_details["export_toolchain"] != EXPORT_TOOLCHAIN.JAVA:
            fail(
                "Only Java binaries are currently supported. Encountered: %s." % (
                    export_details["export_toolchain"]
                ),
            )
        java_binary(
            name = export_details["exposed_artifact_name"],
            deps = export_details["additional_exports"],
            visibility = base_visibility,
            runtime_deps = [
                "@%s//%s" % (name, export_details["exportable_runtime_target"]),
            ],
            main_class = export_details["main_class"],
        )
    else:
        fail("Unsupported export type: %s." % export_details["export_type"])

def _extract_maven_dependencies(artifact_tree):
    """
    Returns a list of Maven dependency artifacts to install to fulfill all third-party dependencies.
    """
    return (
        _create_all_maven_deps(artifact_tree["deps"]["prod"], test_only = False) +
        _create_all_maven_deps(artifact_tree["deps"]["test"], test_only = True)
    )

def _create_all_maven_deps(deps_metadata, test_only):
    """
    Returns a list of Maven dependency artifacts to install to fulfill specific third-party
    dependencies.
    """
    exclusions = deps_metadata.get("exclusions")
    return (
        _create_maven_deps(deps_metadata["direct"], exclusions, test_only) +
        _create_maven_deps(deps_metadata["transitive"], exclusions, test_only)
    )

def _create_maven_deps(dependency_versions, exclusions, test_only):
    return [
        _create_maven_artifact(name, version, exclusions, test_only)
        for name, version in dependency_versions.items()
    ]

def _create_maven_artifact(name, version, exclusions, test_only):
    # Create production & test specific dependencies per:
    # https://github.com/bazelbuild/rules_jvm_external#test-only-dependencies.
    coordinate = parse.parse_maven_coordinate("%s:%s" % (name, version))
    return maven.artifact(
        coordinate["group"],
        coordinate["artifact"],
        coordinate["version"],
        packaging = coordinate.get("packaging"),
        exclusions = exclusions,
        testonly = test_only,
    )
