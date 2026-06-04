"""
Rules for downloading production assets using the download lessons script from external repository.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")

def _download_prod_assets_impl(ctx):
    output_dir = ctx.actions.declare_directory(ctx.attr.name + "_assets")

    api_key_path = ctx.attr.proto_api_key_file[BuildSettingInfo].value
    if not api_key_path:
        fail("Must provide --//config:proto_api_key_file when building with prod assets (e.g. --//config:proto_api_key_file=/path/to/secret).")

    inputs = [ctx.file.pinned_versions, ctx.file.download_config]

    # Declare a temporary directory inside the execution root
    tmp_out = output_dir.path + "_tmp"

    # Positional arguments for the download tool (writing output to tmp_out)
    tool_arguments = [
        ctx.attr.base_url,
        ctx.attr.gcs_base_url,
        ctx.attr.gcs_bucket,
        api_key_path,
        tmp_out, # output_dir argument of the tool points to tmp_out!
        "cache_mode=lazy",
        ctx.attr.name + "_cache", # cache_dir path
        ctx.file.pinned_versions.path,
        ctx.file.download_config.path,
        "true" if ctx.attr.download_questions else "false",
    ]
    if ctx.attr.strict:
        tool_arguments.append("-Werror")

    # Write the wrapper bash script to download and filter assets
    script_content = """#!/bin/bash
set -e
TOOL_PATH="$1"
shift

TMP_OUT="{tmp_out}"
FINAL_OUT="{final_out}"

cleanup() {{
  rm -rf "$TMP_OUT"
}}
trap cleanup EXIT

# Ensure clean temp dir
rm -rf "$TMP_OUT"
mkdir -p "$TMP_OUT"

# Run the download tool (unmodified from external repo)
"$TOOL_PATH" "$@"

# Create final output directories
mkdir -p "$FINAL_OUT"

# Copy v1 binary protos if they exist
if [ -d "$TMP_OUT/protov1/binary" ] && [ "$(ls -A "$TMP_OUT/protov1/binary")" ]; then
  cp "$TMP_OUT"/protov1/binary/*.pb "$FINAL_OUT"/
fi

# Copy images if they exist
if [ -d "$TMP_OUT/images" ] && [ "$(ls -A "$TMP_OUT/images")" ]; then
  mkdir -p "$FINAL_OUT/images"
  cp -r "$TMP_OUT"/images/* "$FINAL_OUT"/images/
fi
""".format(
        tmp_out = tmp_out,
        final_out = output_dir.path,
    )

    ctx.actions.run_shell(
        outputs = [output_dir],
        inputs = inputs,
        tools = [ctx.executable._download_tool],
        command = script_content,
        arguments = [ctx.executable._download_tool.path] + tool_arguments,
        mnemonic = "DownloadProdAssets",
        progress_message = "Downloading, filtering and preparing production assets",
        execution_requirements = {
            "no-sandbox": "1",
            "requires-network": "1",
            "local": "1",
        },
    )

    return [
        DefaultInfo(files = depset([output_dir])),
    ]

download_prod_assets = rule(
    implementation = _download_prod_assets_impl,
    attrs = {
        "base_url": attr.string(default = "https://oppiaserver.appspot.com"),
        "gcs_base_url": attr.string(default = "https://storage.googleapis.com"),
        "gcs_bucket": attr.string(default = "oppiaserver-resources"),
        "pinned_versions": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "download_config": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "strict": attr.bool(default = True),
        "download_questions": attr.bool(default = False),
        "proto_api_key_file": attr.label(default = "//config:proto_api_key_file"),
        "_download_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "@oppia_android_asset_pipeline//scripts:download_lessons",
        ),
    },
)
