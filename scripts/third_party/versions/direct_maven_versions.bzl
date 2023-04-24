"""
Contains all of the Maven dependencies that required to build Oppia Android scripts and their test
targets. These are exposed via PRODUCTION_DEPENDENCY_VERSIONS and TEST_DEPENDENCY_VERSIONS.

Note that all versions listed in this file will automatically be exposed via //scripts/third_party
library wrappers. For example, the "org.jetbrains.kotlinx:kotlinx-coroutines-core" dependency will
be available via //scripts/third_party:org_jetbrains_kotlinx_kotlinx-coroutines-core. Test
dependencies are only visible to tests and test targets. None of these libraries are available to
app builds.

See //third_party/versions/direct_maven_versions.bzl for additional details on these file and how to
update it.
"""

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should contain only production (non-test) dependencies.
PRODUCTION_DEPENDENCY_VERSIONS = {
    "com.android.tools.apkparser:apkanalyzer": "30.0.4",
    "com.android.tools.build:aapt2-proto": "7.3.1-8691043",
    "com.google.guava:guava": "31.0.1-jre",
    "com.google.protobuf:protobuf-java": "3.17.3",
    "com.squareup.moshi:moshi": "1.13.0",
    "com.squareup.moshi:moshi-kotlin-codegen": "1.13.0",
    "org.jetbrains.kotlin:kotlin-compiler-embeddable": "1.5.0",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8": "1.6.21",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core": "1.4.1",
}

# Note to developers: Please keep this dict sorted by key to make it easier to find dependencies.
# This list should only contain test-only dependencies. These are dependencies that are guaranteed
# cannot be included in production builds of scripts.
TEST_DEPENDENCY_VERSIONS = {
    "com.google.truth.extensions:truth-liteproto-extension": "1.1.3",
    "com.google.truth:truth": "1.1.3",
    "junit:junit": "4.13.2",
    "org.mockito.kotlin:mockito-kotlin": "3.2.0",
    "org.mockito:mockito-core": "3.9.0",
}
