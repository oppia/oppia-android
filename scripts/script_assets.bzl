"""
Macro for retrieving the proto assets for script checks.
"""

load("//model:text_proto_assets.bzl", "gen_binary_proto_from_text", "generate_proto_binary_assets")
load("//third_party:system_images_list.bzl", "REQUESTED_SYSTEM_IMAGE_LIST", "SYSTEM_IMAGES_LIST")

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

def generate_device_hardware_profiles_text_protos(
        name,
        device_hardware_profiles):
    """
    Converts a single list of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        device_hardware_profiles: list of str. The list of textprotos to convert.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = device_hardware_profiles,
        proto_dep_name = "device_configurations",
        proto_type_name = "DeviceHardwareProfiles",
        name_prefix = "device_hardware_profiles",
        asset_dir = "assets",
        proto_dep_bazel_target_prefix = "//scripts/src/java/org/oppia/android/scripts/proto",
        proto_package = "proto",
    )

def _generate_available_system_images_text_proto_impl(ctx):
    output_file = ctx.outputs.output_file

    # Generate a parsable textproto based on the available system images list.
    text_proto_lines = []
    for requested_image_path in REQUESTED_SYSTEM_IMAGE_LIST:
        text_proto_lines.append("requested_system_image: \"%s\"" % requested_image_path)
    for path, os_split_definitions in SYSTEM_IMAGES_LIST.items():
        text_proto_lines.append("available_system_image {")
        text_proto_lines.append("  key: \"%s\"" % path)
        text_proto_lines.append("  value {")
        for os_name, definition in os_split_definitions.items():
            text_proto_lines.append("    os_split_images {")
            text_proto_lines.append("      %s {" % os_name)
            text_proto_lines.append("        url: \"%s\"" % definition["url"])
            text_proto_lines.append("        sha1: \"%s\"" % definition["sha1"])
            text_proto_lines.append("        sha256: \"%s\"" % definition["sha256"])
            text_proto_lines.append("      }")
            text_proto_lines.append("    }")
        text_proto_lines.append("  }")
        text_proto_lines.append("}")
    text_proto_contents = "\n".join(text_proto_lines)
    ctx.actions.write(output_file, text_proto_contents)

    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

_generate_available_system_images_text_proto = rule(
    attrs = {
        "output_file": attr.output(
            mandatory = True,
        ),
    },
    implementation = _generate_available_system_images_text_proto_impl,
)

def generate_available_system_images_text_proto(name):
    """
    Generates a textproto for available system images, then converts it to a binary proto.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.

    Returns:
        str. The path to the newly generated binary file.
    """
    _generate_available_system_images_text_proto(
        name = "%s_generate_textproto" % name,
        output_file = "%s_generated.textproto" % name,
    )

    return gen_binary_proto_from_text(
        name = "generate_binary_proto_for_text_proto_%s" % name,
        input_file = "%s_generate_textproto" % name,
        output_file = "%s.pb" % name,
        proto_deps = [
            "//scripts/src/java/org/oppia/android/scripts/proto:device_configurations_proto",
        ],
        proto_type_name = "proto.AvailableSystemImages",
    )
