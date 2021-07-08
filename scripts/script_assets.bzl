"""
Macro for retrieving the proto assets for script checks.
"""

load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

def generate_regex_assets_list_from_text_protos(
        name,
        filepath_pattern_validation_file_names,
        file_content_validation_file_names):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        filepath_pattern_validation_file_names: list of str. The list of prohibited filepath pattern file names.
        file_content_validation_file_names: list of str. The list of prohibited file contents file names.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = filepath_pattern_validation_file_names,
        proto_dep_name = "filename_pattern_validation_checks",
        proto_type_name = "FilenameChecks",
        name_prefix = name,
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    ) + generate_proto_binary_assets(
        name = name,
        names = file_content_validation_file_names,
        proto_dep_name = "file_content_validation_checks",
        proto_type_name = "FileContentChecks",
        name_prefix = name,
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def generate_test_file_assets_list_from_text_protos(
        name,
        test_file_exemptions_name):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        test_file_exemptions_name: list of str. The list of test file exemptions file names.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = test_file_exemptions_name,
        proto_dep_name = "script_exemptions",
        proto_type_name = "ScriptExemptions",
        name_prefix = name,
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )
