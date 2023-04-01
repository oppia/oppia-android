"""
Macro for generating proto assets for app-wide configurations.
"""

load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

def generate_supported_languages_configuration_from_text_proto(
        name,
        supported_language_text_proto_file_name):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        supported_language_text_proto_file_name: target. The target corresponding to the text proto
            defining the list of supported languages in the app.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = [supported_language_text_proto_file_name],
        proto_dep_name = "languages",
        proto_type_name = "SupportedLanguages",
        name_prefix = "supported_languages",
        asset_dir = "languages",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    )
