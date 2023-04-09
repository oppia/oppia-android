"""
Provides the high-level configuration required for downloading & accessing dependencies from Maven,
via MAVEN_ARTIFACT_CONFIGURATION.

This file generally never needs to be changed directly. Instead, Maven dependencies can be updated
and/or added/removed via direct_maven_versions.bzl.
"""

load(
    "//third_party/macros:direct_dep_defs.bzl",
    "create_dep_config",
    "create_maven_artifact_configuration",
)
load(":direct_maven_versions.bzl", "PRODUCTION_DEPENDENCY_VERSIONS", "TEST_DEPENDENCY_VERSIONS")
load(
    ":transitive_maven_versions.bzl",
    "PRODUCTION_TRANSITIVE_DEPENDENCY_VERSIONS",
    "TEST_TRANSITIVE_DEPENDENCY_VERSIONS",
)

MAVEN_ARTIFACT_CONFIGURATION = create_maven_artifact_configuration(
    production_dep_config = create_dep_config(
        direct_deps = PRODUCTION_DEPENDENCY_VERSIONS,
        transitive_deps = PRODUCTION_TRANSITIVE_DEPENDENCY_VERSIONS,
    ),
    test_dep_config = create_dep_config(
        direct_deps = TEST_DEPENDENCY_VERSIONS,
        transitive_deps = TEST_TRANSITIVE_DEPENDENCY_VERSIONS,
    ),
    maven_install_json_target = "//scripts/third_party/versions:maven_install.json",
)
