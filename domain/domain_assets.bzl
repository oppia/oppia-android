"""
Macros for preparing & creating assets to include in the domain layer.
"""

load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("//:build_vars.bzl", "BUILD_SDK_VERSION")
load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

def _generate_assets_list_from_text_protos(
        name,
        classroom_file_names,
        classroom_list_file_names,
        topic_file_names,
        subtopic_file_names,
        story_file_names,
        skills_file_names,
        exploration_file_names,
        asset_dir):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        classroom_file_names: list of str. The list of classroom file names.
        classroom_list_file_names: list of str. The classroom list file names.
        topic_file_names: list of str. The list of topic file names.
        subtopic_file_names: list of str. The list of subtopic file names.
        story_file_names: list of str. The list of story file names.
        skills_file_names: list of str. The list of skills/concept card list file names.
        exploration_file_names: list of str. The list of exploration file names.
        asset_dir: str. The directory where the textproto files are located.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = classroom_list_file_names,
        proto_dep_name = "topic",
        proto_type_name = "ClassroomIdList",
        name_prefix = "classroom_id_list",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = classroom_file_names,
        proto_dep_name = "topic",
        proto_type_name = "ClassroomRecord",
        name_prefix = "classroom_record",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = topic_file_names,
        proto_dep_name = "topic",
        proto_type_name = "TopicRecord",
        name_prefix = "topic_record",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = subtopic_file_names,
        proto_dep_name = "topic",
        proto_type_name = "SubtopicRecord",
        name_prefix = "subtopic_record",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = story_file_names,
        proto_dep_name = "topic",
        proto_type_name = "StoryRecord",
        name_prefix = "story_record",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = skills_file_names,
        proto_dep_name = "topic",
        proto_type_name = "ConceptCardList",
        name_prefix = "concept_card_list",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = exploration_file_names,
        proto_dep_name = "exploration",
        proto_type_name = "Exploration",
        name_prefix = "exploration",
        asset_dir = asset_dir,
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    )

def local_assets_library(
        name,
        assets_dir,
        classroom_file_names = [],
        classroom_list_file_names = [],
        topic_file_names = [],
        subtopic_file_names = [],
        story_file_names = [],
        skills_file_names = [],
        exploration_file_names = [],
        json_assets = [],
        **kwargs):
    """
    Creates an android_library that packages locally defined assets.

    This converts provided textproto assets to binary and also supports including JSON assets.

    Args:
        name: str. The name of for the library being defined.
        assets_dir: str. The directory containing the assets.
        classroom_file_names: list of str. The list of classroom file names.
        classroom_list_file_names: list of str. The classroom list file names.
        topic_file_names: list of str. The list of topic file names.
        subtopic_file_names: list of str. The list of subtopic file names.
        story_file_names: list of str. The list of story file names.
        skills_file_names: list of str. The list of skills/concept card list file names.
        exploration_file_names: list of str. The list of exploration file names.
        json_assets: list of label. The list of physical JSON asset files to package.
        **kwargs: additional parameters to pass to android_library.
    """
    manifest_target = "_%s_manifest" % name
    manifest_file = "_%s_AndroidManifest.xml" % name

    # Generate a simple AndroidManifest.xml for this asset library.
    write_file(
        name = manifest_target,
        out = manifest_file,
        content = [
            '<?xml version="1.0" encoding="utf-8"?>',
            '<manifest xmlns:android="http://schemas.android.com/apk/res/android"',
            '    package="org.oppia.android.domain.assets.%s">' % name,
            '    <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="%d" />' % BUILD_SDK_VERSION,
            "</manifest>",
        ],
    )

    generated_assets = _generate_assets_list_from_text_protos(
        name = "%s_generation" % name,
        classroom_file_names = classroom_file_names,
        classroom_list_file_names = classroom_list_file_names,
        topic_file_names = topic_file_names,
        subtopic_file_names = subtopic_file_names,
        story_file_names = story_file_names,
        skills_file_names = skills_file_names,
        exploration_file_names = exploration_file_names,
        asset_dir = assets_dir,
    )

    native.android_library(
        name = name,
        assets = json_assets + generated_assets,
        assets_dir = assets_dir + "/",
        manifest = manifest_file,
        **kwargs
    )
