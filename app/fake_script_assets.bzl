"""
Macros for preparing & creating assets to include in the scripts module.
"""

load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

def generate_assets_list_from_text_protos(
        name,
        maven_dependencies_file_name):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        maven_dependencies_file_name: list of str. The list of prohibited filepath pattern file names.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = maven_dependencies_file_name,
        proto_dep_name = "generate_maven_dependencies_list",
        proto_type_name = "MavenDependencyList",
        name_prefix = name,
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//app/src/main/java/org/oppia/android/app/maven/proto",
        proto_package = "proto",
    )
