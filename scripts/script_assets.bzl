load("//domain:domain_assets.bzl", "gen_binary_proto_from_text")

def _generate_single_asset_proto_binary(name, proto_file_name, proto_dep_name, proto_type_name):
    """
    Converts a single asset text proto to a new binary asset.

    Args:
        name: str. The name of this target.
        proto_file_name: str. The file name of the text proto under the assets directory that will
            be converted. This is assuming to correspond to 'src/main/assets/<name>.textproto' and
            will lead to a new generated file called 'src/main/assets/<name>.pb'.
        proto_dep_name: str. The name of the proto library under //model that contains the proto
            definition being converted to binary.
        proto_type_name: str. The name of the proto type being converted in the text proto. This is
            assumed to be part of the shared 'model' package.

    Returns:
        str. The path to the newly generated binary file.
    """
    asset_dir = "assets"
    return gen_binary_proto_from_text(
        name = "generate_binary_proto_for_text_proto_%s" % name,
        input_file = "%s/%s.textproto" % (asset_dir, proto_file_name),
        output_file = "%s/%s.pb" % (asset_dir, proto_file_name),
        proto_deps = [
            "//scripts/src/main/java/org/oppia/android/scripts/proto:%s_proto" % proto_dep_name,
        ],
        proto_type_name = "model.%s" % proto_type_name,
    )

def _generate_proto_binary_assets(names, proto_dep_name, proto_type_name, name_prefix):
    """
    Converts a list of text proto assets to binary.

    Args:
        names: list of str. The list of text proto file names under the assets directory that should
            be converted.
        proto_dep_name: str. See _generate_single_asset_proto_binary.
        proto_type_name: str. See _generate_single_asset_proto_binary.
        name_prefix: str. A prefix to attach to the name of this target.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return [
        _generate_single_asset_proto_binary(
            name = "%s_%s" % (name_prefix, name),
            proto_file_name = name,
            proto_dep_name = proto_dep_name,
            proto_type_name = proto_type_name,
        )
        for name in names
    ]

def generate_assets_list_from_text_protos(
        name,
        filename_validation_file_names,
        file_content_validation_file_names):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        filename_validation_file_names: list of str. The list of prohibited filename pattern file names.
        file_content_validation_file_names: list of str. The list of prohibited file contents file names.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return _generate_proto_binary_assets(
        names = filename_validation_file_names,
        proto_dep_name = "filename_pattern_validation_structure",
        proto_type_name = "FilenameChecks",
        name_prefix = name,
    ) + _generate_proto_binary_assets(
        names = file_content_validation_file_names,
        proto_dep_name = "file_content_validation_structure",
        proto_type_name = "FileContentChecks",
        name_prefix = name,
    )
