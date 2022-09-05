"""
Macros for preparing & creating assets to include in the domain module.
"""

load("//model:text_proto_assets.bzl", "generate_proto_binary_assets")

def _compute_stripped_path(input_file, base_path):
    short_path = input_file.short_path
    if not short_path.startswith(base_path):
        fail("Expected %s input file to have base path: %s", input_file, base_path)
    return short_path[len(base_path):]

# TODO: Add TODO to remove this once Gradle goes away since the assets can just become top-level.
def _copy_files_impl(ctx):
    input_files = ctx.files.input_files
    input_base_path = ctx.attr.input_base_path
    output_file_path = ctx.attr.output_file_path

    output_files = [
        ctx.actions.declare_file(
            "%s/%s" % (output_file_path, _compute_stripped_path(input_file, input_base_path)),
        )
        for input_file in input_files
    ]

    # Use a symlink to avoid an actual filesystem copy (for compatible systems).
    for (input_file, output_file) in zip(input_files, output_files):
        ctx.actions.symlink(output = output_file, target_file = input_file)

    return DefaultInfo(
        files = depset(output_files),
        runfiles = ctx.runfiles(files = output_files),
    )

_copy_files = rule(
    attrs = {
        "input_files": attr.label_list(
            allow_empty = False,
            allow_files = True,
            mandatory = True,
        ),
        "input_base_path": attr.string(mandatory = True),
        "output_file_path": attr.string(mandatory = True),
    },
    implementation = _copy_files_impl,
)

# TODO: Mention in documentation that input_base_path needs to end with '/'.
def copy_files(name, input_files, input_base_path, output_file_path):
    _copy_files(
        name = name,
        input_files = input_files,
        input_base_path = input_base_path,
        output_file_path = output_file_path,
    )
    return [":%s" % name]

def generate_assets_list_from_text_protos(
        name,
        topic_list_file_names,
        topic_file_names,
        subtopic_file_names,
        story_file_names,
        skills_file_names,
        exploration_file_names):
    """
    Converts multiple lists of text proto assets to binary.

    Args:
        name: str. The name of this generation instance. This will be a prefix for derived targets.
        topic_list_file_names: list of str. The list of topic list file names.
        topic_file_names: list of str. The list of topic file names.
        subtopic_file_names: list of str. The list of subtopic file names.
        story_file_names: list of str. The list of story file names.
        skills_file_names: list of str. The list of skills/concept card list file names.
        exploration_file_names: list of str. The list of exploration file names.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return generate_proto_binary_assets(
        name = name,
        names = topic_list_file_names,
        proto_dep_name = "topic",
        proto_type_name = "TopicIdList",
        name_prefix = "topic_id_list",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = topic_file_names,
        proto_dep_name = "topic",
        proto_type_name = "TopicRecord",
        name_prefix = "topic_record",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = subtopic_file_names,
        proto_dep_name = "topic",
        proto_type_name = "SubtopicRecord",
        name_prefix = "subtopic_record",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = story_file_names,
        proto_dep_name = "topic",
        proto_type_name = "StoryRecord",
        name_prefix = "story_record",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = skills_file_names,
        proto_dep_name = "topic",
        proto_type_name = "ConceptCardList",
        name_prefix = "concept_card_list",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    ) + generate_proto_binary_assets(
        name = name,
        names = exploration_file_names,
        proto_dep_name = "exploration",
        proto_type_name = "Exploration",
        name_prefix = "exploration",
        asset_dir = "src/main/assets",
        proto_dep_bazel_target_prefix = "//model/src/main/proto",
        proto_package = "model",
    )

# TODO: Revert related changes.
def retrieve_domain_assets(name, dest_assets_dir):
    return copy_files(
        name = "copy_domain_assets_%s" % name,
        input_files = ["//domain:domain_assets"],
        input_base_path = "domain/src/main/assets/",
        output_file_path = dest_assets_dir,
    )
