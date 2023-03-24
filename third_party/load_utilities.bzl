load(":versions.bzl", "MAVEN_ARTIFACT_TREES")
load("@rules_jvm_external//:defs.bzl", "artifact")

# TODO: Add docs for this func, other functions, and the file.

# Create android library wrappers for select dependencies. Note that artifact is used so that the
# correct Maven repository is selected. Also, dependencies are restricted to specific visibility
# scopes (e.g. app vs. scripts), or to specific build contexts (e.g. tests) to help ensure
# cross-dependencies can't accidentally occur.

def create_dependency_wrappers(build_context, artifact_visibility):
    artifact_tree = MAVEN_ARTIFACT_TREES[build_context]
    for name, version in artifact_tree["deps"].items():
        native.android_library(
            name = name.replace(":", "_").replace(".", "_"),
            testonly = artifact_tree["test_only"],
            visibility = artifact_visibility,
            exports = [artifact(
                "%s:%s" % (name, version),
                repository_name = "maven_%s" % build_context,
            )],
        )
