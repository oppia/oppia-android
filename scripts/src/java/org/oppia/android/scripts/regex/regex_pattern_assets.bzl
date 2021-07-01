"""
Macro for retrieving the regex pattern check assets.
"""

load("//scripts:script_assets.bzl", "generate_assets_list_from_text_protos")

def retrieve_regex_pattern_check_assets(name, asset_dir):
    """
    Converts a single asset text proto to a new binary asset.

    Args:
        name: str. The name of this target.
        asset_dir: str. The path to the assets directory where the textproto files are present.
            Example: 'src/main/assets'

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_assets_list_from_text_protos(
       name = name,
       file_content_validation_file_names = [
           "file_content_validation_checks",
       ],
       filepath_pattern_validation_file_names = [
           "filename_pattern_validation_checks",
       ]
   )
