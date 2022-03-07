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
        name_prefix = "filename_checks",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    ) + generate_proto_binary_assets(
        name = name,
        names = file_content_validation_file_names,
        proto_dep_name = "file_content_validation_checks",
        proto_type_name = "FileContentChecks",
        name_prefix = "file_content_checks",
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
        test_file_exemptions_name: list of str. The list of test file exemptions file name.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = test_file_exemptions_name,
        proto_dep_name = "script_exemptions",
        proto_type_name = "TestFileExemptions",
        name_prefix = "test_file_exemptions",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def generate_maven_assets_list_from_text_protos(
        name,
        maven_dependency_filenames):
    """
    Converts a single list of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        maven_dependency_filenames: The list of maven_dependencies text proto file names under the
            assets directory that should be converted.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = maven_dependency_filenames,
        proto_dep_name = "maven_dependencies",
        proto_type_name = "MavenDependencyList",
        name_prefix = "maven_dependency_list",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def generate_accessibility_label_assets_list_from_text_protos(
        name,
        accessibility_label_exemptions_name):
    """
    Converts a single list of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        accessibility_label_exemptions_name: list of str. The list of accessibility label exemptions
         file name.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = accessibility_label_exemptions_name,
        proto_dep_name = "script_exemptions",
        proto_type_name = "AccessibilityLabelExemptions",
        name_prefix = "accessibility_label_exemptions",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def generate_kdoc_validity_assets_list_from_text_protos(
        name,
        kdoc_validity_exemptions_name):
    """
    Converts a single list of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        kdoc_validity_exemptions_name: list of str. The list of kdoc validity exemptions file name.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = kdoc_validity_exemptions_name,
        proto_dep_name = "script_exemptions",
        proto_type_name = "KdocValidityExemptions",
        name_prefix = "kdoc_validity_exemptions",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def generate_todo_assets_list_from_text_protos(
        name,
        todo_exemptions_name):
    """
    Converts a single list of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        todo_exemptions_name: list of str. The list of todo exemptions file name.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = todo_exemptions_name,
        proto_dep_name = "script_exemptions",
        proto_type_name = "TodoOpenExemptions",
        name_prefix = "todo_open_exemptions",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )
