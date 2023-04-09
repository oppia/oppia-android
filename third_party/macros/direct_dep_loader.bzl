"""
Provides Starlark macros for importing and wrapping both Maven-hosted and directly referenced remote
dependencies.
"""

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_import", "kt_jvm_library")
load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven", "parse")
load(":direct_dep_defs.bzl", "EXPORT_TOOLCHAIN", "EXPORT_TYPE")

def download_maven_dependencies(name, maven_artifact_config, maven_repositories):
    """Loads the Maven-hosted dependencies needed to be able to build the Oppia Android project.

    Note that this must be called after loading in this deps file, for example:

        load("//third_party/macros:direct_dep_loader.bzl", "download_maven_dependencies")
        download_maven_dependencies()

    Note that this can only be called after toolchains are set up using
    //third_party/tools:toolchains.bzl as it requires supporting rules_jvm_external.

    Args:
        name: str. The name of the Maven repository that's being set up, per rules_jvm_external's
            maven_install macro.
        maven_artifact_config: dict. The create_maven_artifact_configuration()-created configuration
            specifying dependencies that should be downloaded into a new locally-available Maven
            repository.
        maven_repositories: list of str. The list of Maven repository URLs that should be used when
            downloading direct dependencies which are pulled from Maven.
    """

    # Add support for JVM rules: https://github.com/bazelbuild/rules_jvm_external. Note to
    # developers: new dependencies should be added to third_party/versions.bzl, not here.
    maven_install(
        name = name,
        artifacts = _extract_maven_dependencies(maven_artifact_config),
        duplicate_version_warning = "error",
        fail_if_repin_required = True,
        maven_install_json = maven_artifact_config["maven_install_json"],
        override_targets = maven_artifact_config.get("target_overrides") or {},
        repositories = maven_repositories,
        strict_visibility = True,
    )

def create_direct_import_dependency_wrappers(
        dependency_imports_details,
        prod_artifact_visibility,
        test_artifact_visibility,
        maven_artifact_visibility):
    """Creates wrapper libraries & binaries for directly imported dependencies.

    Note that the new targets will be made available in the immediate package, so this should be
    called in the BUILD file corresponding to the package that should house these wrappers.

    Args:
        dependency_imports_details: list of dict. The list of direct dependency configurations that
            should each be wrapped (for configurations that include export details). Each of these
            dictionaries is expected to have been created from one of the create_*_reference()
            macros in direct_dep_defs.bzl.
        prod_artifact_visibility: list of str. The list of Bazel visibility rules that should
            automatically be applied to all non-test/production dependencies (i.e. the visibilities
            of the new wrappers).
        test_artifact_visibility: list of str. The list of Bazel visibility rules that should
            automatically be applied to all test-only dependencies (i.e. the visibilities of the new
            wrappers).
        maven_artifact_visibility: list of str. The list of Bazel visibility rules that should
            automatically be applied to all dependency wrappers that have been configured to be
            available to Maven dependencies (to ensure that Maven dependencies themselves can access
            the new wrapper library, such as in the case of overrides).
    """
    for import_details in dependency_imports_details:
        for export_details in import_details["exports_details"]:
            _wrap_dependency(
                import_details,
                export_details,
                prod_artifact_visibility,
                test_artifact_visibility,
                maven_artifact_visibility,
            )

# buildifier: disable=unnamed-macro
def create_maven_dependency_wrappers(
        maven_repository_name,
        maven_artifact_config,
        prod_artifact_visibility,
        test_artifact_visibility):
    """Creates wrapper libraries for Maven-imported dependencies.

    Args:
        maven_repository_name: str. The name of the Maven repository from which dependencies will be
            wrapped (as passed to download_maven_dependencies()).
        maven_artifact_config: dict. The create_maven_artifact_configuration()-created configuration
            specifying dependencies that should be wrapped into new libraries.
        prod_artifact_visibility: list of str. The list of Bazel visibility rules that should
            automatically be applied to all non-test/production dependencies (i.e. the visibilities
            of the new wrappers).
        test_artifact_visibility: list of str. The list of Bazel visibility rules that should
            automatically be applied to all test-only dependencies (i.e. the visibilities of the new
            wrappers).
    """
    _wrap_maven_dependencies(
        maven_repository_name,
        maven_artifact_config["deps"]["prod"]["direct"],
        prod_artifact_visibility,
        test_only = False,
    )
    _wrap_maven_dependencies(
        maven_repository_name,
        maven_artifact_config["deps"]["test"]["direct"],
        test_artifact_visibility,
        test_only = True,
    )

def _wrap_maven_dependencies(
        maven_repository_name,
        dependency_versions,
        artifact_visibility,
        test_only):
    # Create android library wrappers for select dependencies. Note that artifact is used so that
    # the correct Maven repository is selected. Also, dependencies are restricted to specific
    # visibility scopes (e.g. prod vs. test), or to specific build contexts (e.g. scripts which is
    # in an entirely different target path) to help prevent accidental cross-dependencies.
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

def _extract_maven_dependencies(artifact_config):
    """Returns a list of Maven dependency artifacts to install to fulfill all third-party deps.
    """
    return (
        _create_all_maven_deps(artifact_config["deps"]["prod"], test_only = False) +
        _create_all_maven_deps(artifact_config["deps"]["test"], test_only = True)
    )

def _create_all_maven_deps(deps_metadata, test_only):
    """Returns a list of Maven dependency artifacts to install to fulfill specific third-party deps.
    """
    return (
        _create_maven_deps(deps_metadata["direct"], deps_metadata["exclusions"], test_only) +
        _create_maven_deps(deps_metadata["transitive"], deps_metadata["exclusions"], test_only)
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
