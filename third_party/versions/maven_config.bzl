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
        exclusions = [
            # Don't allow production dependencies to use kotlin-reflect. Note that this may
            # result in runtime failures for libraries that depend on it, so care must be
            # taken or those libraries can't be used as dependencies.
            "org.jetbrains.kotlin:kotlin-reflect",
        ],
    ),
    test_dep_config = create_dep_config(
        direct_deps = TEST_DEPENDENCY_VERSIONS,
        transitive_deps = TEST_TRANSITIVE_DEPENDENCY_VERSIONS,
    ),
    maven_install_json_target = "//third_party/versions:maven_install.json",
    target_overrides = {
        "com.google.guava:guava": "@//third_party:com_google_guava_guava",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm": "@//third_party:kotlinx-coroutines-core-jvm",
    },
)
