"""
Macros for preparing & creating textproto-converted assets to include in any module.
"""

load("@rules_proto//proto:defs.bzl", "ProtoInfo")

def _extract_proto_sources(deps):
    """
    Returns the list of proto source files that make up the specified list of proto dependencies.

    The returned list includes transitive dependencies.
    """

    # See https://github.com/bazelbuild/rules_proto/pull/77/files &
    # https://github.com/bazelbuild/rules_proto/issues/57 &
    # https://docs.bazel.build/versions/master/skylark/lib/ProtoInfo.html for references.
    combined_sources = []
    for dep in deps:
        combined_sources.extend(dep[ProtoInfo].transitive_sources.to_list())
    return combined_sources

def _gen_binary_proto_from_text_impl(ctx):
    # See: https://docs.bazel.build/versions/master/skylark/lib/actions.html#declare_file.
    output_file = ctx.outputs.output_file
    input_file = ctx.attr.input_file.files.to_list()[0].path
    input_proto_files = _extract_proto_sources(ctx.attr.proto_deps)

    # See 'protoc --help' for specifics on the arguments passed to the tool for converting text
    # proto to binary, and expected stdin/stdout configurations. Note that the actual proto files
    # are passed to the compiler since it requires them in order to transcode the text proto file.
    command_path = ctx.executable._protoc_tool.path
    proto_directory_path_args = ["--proto_path=%s" % file.dirname for file in input_proto_files]
    proto_file_names = [file.basename for file in input_proto_files]
    arguments = [command_path] + [
        "--encode %s" % ctx.attr.proto_type_name,
    ] + proto_directory_path_args + proto_file_names + [
        "< %s" % input_file,
        "> %s" % output_file.path,
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run. This
    # actually runs the proto compiler to perform the conversion. Note that this needs to use
    # run_shell() instead of run() because it requires input redirection.
    ctx.actions.run_shell(
        outputs = [output_file],
        inputs = ctx.files.input_file + input_proto_files,
        tools = [ctx.executable._protoc_tool],
        command = " ".join(arguments),
        mnemonic = "GenerateBinaryProtoFromText",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

# Custom Starlark rule for running the proto compiler in encode mode to convert a text proto to
# binary. The custom rule allows this to be done as part of the build graph so that binary files
# never need to be checked into the repository.
_gen_binary_proto_from_text = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "proto_deps": attr.label_list(
            allow_empty = False,
            mandatory = True,
        ),
        "proto_type_name": attr.string(mandatory = True),
        "_protoc_tool": attr.label(
            # This was partly inspired by https://stackoverflow.com/a/39138074.
            executable = True,
            cfg = "host",
            default = "@protobuf_tools//:protoc",
        ),
    },
    implementation = _gen_binary_proto_from_text_impl,
)

def gen_binary_proto_from_text(name, proto_type_name, input_file, output_file, proto_deps):
    """
    Generates a binary proto from a text proto.

    Args:
        name: str. A unique name to identify this generation. This can be built directly using Bazel
            like any other build rule.
        proto_type_name: str. The qualified type name of the proto being converted (e.g.
            'model.Exploration').
        input_file: file. The path to the text proto file being converted.
        output_file: file. The output path for the generated binary proto file.
        proto_deps: list of targets. The list of proto_library dependencies that are needed to
            perform the conversion. Generally, only the proto file corresponding to the proto type
            is needed since proto_library automatically pulls in transitive dependencies.

    Returns:
        str. The path to the newly generated binary file (same as output_file).
    """
    _gen_binary_proto_from_text(
        name = name,
        proto_type_name = proto_type_name,
        input_file = input_file,
        output_file = output_file,
        proto_deps = proto_deps,
    )
    return output_file

def _generate_single_asset_proto_binary(
        name,
        proto_file_name,
        proto_dep_name,
        proto_type_name,
        asset_dir,
        proto_dep_bazel_target_prefix,
        proto_package):
    """
    Converts a single asset text proto to a new binary asset.

    Args:
        name: str. The name of this target.
        proto_file_name: str. The file name of the text proto under the assets directory that will
            be converted. This is assuming to correspond to '<asset_dir>/<name>.textproto' and
            will lead to a new generated file called '<asset_dir>/<name>.pb'.
        proto_dep_name: str. The name of the proto library that contains the proto
            definition being converted to binary.
        proto_type_name: str. The name of the proto type being converted in the text proto.
        asset_dir: str. The path to the assets directory where the textproto files are present.
            Example: 'src/main/assets'.
        proto_dep_bazel_target_prefix: str. The path to the library that contains the proto_dep.
            Example: '//model'.
        proto_package: str. The name of the proto package. Example: 'model'.

    Returns:
        str. The path to the newly generated binary file.
    """
    return gen_binary_proto_from_text(
        name = "generate_binary_proto_for_text_proto_%s" % name,
        input_file = "%s/%s.textproto" % (asset_dir, proto_file_name),
        output_file = "%s/%s.pb" % (asset_dir, proto_file_name),
        proto_deps = [
            "%s:%s_proto" % (proto_dep_bazel_target_prefix, proto_dep_name),
        ],
        proto_type_name = "%s.%s" % (proto_package, proto_type_name),
    )

def generate_proto_binary_assets(
        name,
        names,
        proto_dep_name,
        proto_type_name,
        name_prefix,
        asset_dir,
        proto_dep_bazel_target_prefix,
        proto_package):
    """
    Converts a list of text proto assets to binary.

    Args:
        name: str. The name of this target.
        names: list of str. The list of text proto file names under the assets directory that should
            be converted.
        proto_dep_name: str. See _generate_single_asset_proto_binary.
        proto_type_name: str. See _generate_single_asset_proto_binary.
        name_prefix: str. A prefix to attach to the name of this target.
        asset_dir: str. See _generate_single_asset_proto_binary.
        proto_dep_bazel_target_prefix: str. See _generate_single_asset_proto_binary.
        proto_package: str. See _generate_single_asset_proto_binary.

    Returns:
        list of str. The list of new proto binary asset files that were generated.
    """
    return [
        _generate_single_asset_proto_binary(
            name = "%s_%s" % (name_prefix, name),
            proto_file_name = name,
            proto_dep_name = proto_dep_name,
            proto_type_name = proto_type_name,
            asset_dir = asset_dir,
            proto_dep_bazel_target_prefix = proto_dep_bazel_target_prefix,
            proto_package = proto_package,
        )
        for name in names
    ]
