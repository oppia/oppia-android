# TODO(#1532): Rename file to 'BUILD' post-Gradle.
"""
This package contains configuration libraries for defining & tweaking app-wide behavior.
"""

load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

# Language support levels:
# - all: Indicates all languages/regions technically supported by the app, including ones that may
#   not have complete translations. These will be included in prerelease versions of the app.
# - production: Indicates languages for which the team guarantees thorough support (generally nearly
#   100% written translations).
_LANGUAGE_SUPPORT_LEVELS = [
    "all",
    "production",
]

_SUPPORTED_LANGUAGES_CONFIG_ASSETS = {
    support_level: generate_proto_binary_assets(
        name = "%s_supported_languages_config_assets" % support_level,
        asset_dir = "%slanguages" % support_level,
        name_prefix = "%s_supported_languages_config_assets" % support_level,
        names = ["supported_languages"],
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_dep_name = "languages",
        proto_package = "model",
        proto_type_name = "SupportedLanguages",
    )
    for support_level in _LANGUAGE_SUPPORT_LEVELS
}

_SUPPORTED_REGIONS_CONFIG_ASSETS = {
    support_level: generate_proto_binary_assets(
        name = "%s_supported_regions_config_assets" % support_level,
        asset_dir = "%slanguages" % support_level,
        name_prefix = "%s_supported_regions_config_assets" % support_level,
        names = ["supported_regions"],
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_dep_name = "languages",
        proto_package = "model",
        proto_type_name = "SupportedRegions",
    )
    for support_level in _LANGUAGE_SUPPORT_LEVELS
}

[
    android_library(
        name = "%s_languages_config" % support_level,
        assets = _SUPPORTED_LANGUAGES_CONFIG_ASSETS[support_level] +
                 _SUPPORTED_REGIONS_CONFIG_ASSETS[support_level],
        assets_dir = "%slanguages/" % support_level,
        manifest = "AndroidManifest.xml",
        visibility = [
            "//:oppia_binary_visibility",
            "//:oppia_testing_visibility",
        ],
    )
    for support_level in _LANGUAGE_SUPPORT_LEVELS
]
