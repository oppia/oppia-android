"""
Macro for retrieving the regex pattern check assets.
"""

load("//scripts:script_assets.bzl", "generate_assets_list_from_text_protos")

def retrieve_regex_pattern_check_assets(asset_dir):

    return generate_assets_list_from_text_protos(
       name = "script_assets",
       file_content_validation_file_names = [
           "file_content_validation_checks",
       ],
       filepath_pattern_validation_file_names = [
           "filename_pattern_validation_checks",
       ]
   )