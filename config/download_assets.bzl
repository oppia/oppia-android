"""
Rules for downloading production assets and updating pinned lesson versions using tools from the external repository.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")
load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("//:build_vars.bzl", "BUILD_SDK_VERSION")

_COMMON_ATTRS = {
    "base_url": attr.string(default = "https://www.oppia.org"),
    "gcs_base_url": attr.string(default = "https://storage.googleapis.com"),
    "gcs_bucket": attr.string(default = "oppiaserver-resources"),
    "download_config": attr.label(
        allow_single_file = True,
        mandatory = True,
    ),
    "pinned_versions": attr.label(
        allow_single_file = True,
        mandatory = True,
    ),
    "proto_api_key_file": attr.label(
        default = Label("//config:proto_api_key_file"),
    ),
}

def _get_api_key_path(ctx, action_description):
    api_key_path = ctx.attr.proto_api_key_file[BuildSettingInfo].value
    if not api_key_path:
        fail("Must provide --//config:proto_api_key_file when %s (e.g. --//config:proto_api_key_file=/path/to/secret)." % action_description)
    return api_key_path

def _download_prod_assets_impl(ctx):
    output_dir = ctx.actions.declare_directory(ctx.attr.output_dir_name)
    api_key_path = _get_api_key_path(ctx, "building with prod/alpha assets")

    inputs = [ctx.file.pinned_versions, ctx.file.download_config]
    tmp_out = output_dir.path + "_tmp"

    tool_arguments = [
        ctx.attr.base_url,
        ctx.attr.gcs_base_url,
        ctx.attr.gcs_bucket,
        api_key_path,
        tmp_out,
        "cache_mode=lazy",
        ctx.attr.name + "_cache",
        ctx.file.pinned_versions.path,
        ctx.file.download_config.path,
        "true" if ctx.attr.download_questions else "false",
    ]

    # Write the wrapper bash script to download and filter assets
    script_content = """#!/bin/bash
set -e
TOOL_PATH="$1"
shift

TMP_OUT="{tmp_out}"
FINAL_OUT="{final_out}"
LOG_FILE=$(mktemp /tmp/oppia_download_assets.XXXXXX.log)

cleanup() {{
  rm -rf "$TMP_OUT"
}}
trap cleanup EXIT

# Ensure clean temp dir
rm -rf "$TMP_OUT"
mkdir -p "$TMP_OUT"

# Run the download tool (unmodified from external repo)
if ! "$TOOL_PATH" "$@" > "$LOG_FILE" 2>&1; then
  echo "Asset download failed! Tail of log (last 100 lines):"
  echo ""
  tail -n 100 "$LOG_FILE"
  echo ""
  echo "Full log is available at: $LOG_FILE"
  echo ""
  echo "Command line that was run:"
  echo "$TOOL_PATH" "$@"
  exit 1
fi

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
        progress_message = "Downloading/filtering/prepping prod assets",
        execution_requirements = {
            "no-sandbox": "1",
            "requires-network": "1",
            "local": "1",
        },
    )

    return [
        DefaultInfo(files = depset([output_dir])),
    ]

_download_prod_assets = rule(
    implementation = _download_prod_assets_impl,
    attrs = dict({
        "output_dir_name": attr.string(mandatory = True),
        "download_questions": attr.bool(mandatory = True),
        "_download_tool": attr.label(
            default = Label("@oppia_android_asset_pipeline//scripts:download_lessons"),
            executable = True,
            cfg = "exec",
        ),
    }, **_COMMON_ATTRS),
)

def downloaded_assets_library(name, download_config, pinned_versions, download_questions = False, tags = [], visibility = [], **kwargs):
    manifest_target = "_%s_manifest" % name
    manifest_file = "_%s_AndroidManifest.xml" % name
    downloaded_lessons_target = "_%s_downloaded_assets" % name
    asset_dir_name = "_%s_asset_dir" % name

    # Generate a simple AndroidManifest.xml for this asset library.
    write_file(
        name = manifest_target,
        out = manifest_file,
        content = [
            '<?xml version="1.0" encoding="utf-8"?>',
            '<manifest xmlns:android="http://schemas.android.com/apk/res/android"',
            '    package="org.oppia.android.domain.assets.%s">' % name,
            '    <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="%d" />' % BUILD_SDK_VERSION,
            '</manifest>',
        ],
        tags = tags,
    )

    _download_prod_assets(
        name = downloaded_lessons_target,
        output_dir_name = asset_dir_name,
        download_config = download_config,
        pinned_versions = pinned_versions,
        download_questions = download_questions,
        tags = tags,
        **kwargs
    )

    native.android_library(
        name = name,
        assets = [downloaded_lessons_target],
        assets_dir = asset_dir_name,
        manifest = manifest_file,
        tags = tags,
        visibility = visibility,
    )

def _update_pinned_lesson_versions_impl(ctx):
    api_key_path = _get_api_key_path(ctx, "running update_pinned_lesson_versions")

    script = ctx.actions.declare_file(ctx.label.name + ".sh")
    tool_path = ctx.executable._download_tool.short_path
    config_path = ctx.file.download_config.short_path
    pinned_versions_path = ctx.file.pinned_versions.short_path

    script_content = """#!/bin/bash
set -e

TOOL="{tool_path}"
CONFIG="{config_path}"
OUTPUT="$BUILD_WORKSPACE_DIRECTORY/{pinned_versions_path}"

echo "Updating pinned lesson versions using GAE server..."
echo "API Key File: {api_key_path}"
echo "Output File: $OUTPUT"
echo "Config File: $CONFIG"

"$TOOL" \
  "{base_url}" \
  "{gcs_base_url}" \
  "{gcs_bucket}" \
  "{api_key_path}" \
  "$OUTPUT" \
  "$CONFIG"

echo "Successfully updated pinned lesson versions!"
""".format(
        tool_path = tool_path,
        config_path = config_path,
        pinned_versions_path = pinned_versions_path,
        base_url = ctx.attr.base_url,
        gcs_base_url = ctx.attr.gcs_base_url,
        gcs_bucket = ctx.attr.gcs_bucket,
        api_key_path = api_key_path,
    )

    ctx.actions.write(
        output = script,
        content = script_content,
        is_executable = True,
    )

    runfiles = ctx.runfiles(files = [
        ctx.executable._download_tool,
        ctx.file.download_config,
    ])
    runfiles = runfiles.merge(ctx.attr._download_tool[DefaultInfo].default_runfiles)

    return [
        DefaultInfo(
            executable = script,
            runfiles = runfiles,
        ),
    ]

_update_pinned_lesson_versions = rule(
    implementation = _update_pinned_lesson_versions_impl,
    executable = True,
    attrs = dict({
        "_download_tool": attr.label(
            default = Label("@oppia_android_asset_pipeline//scripts:download_lesson_list"),
            executable = True,
            cfg = "exec",
        ),
    }, **_COMMON_ATTRS),
)

def update_pinned_lesson_versions(name, download_config, pinned_versions, **kwargs):
    _update_pinned_lesson_versions(
        name = name,
        download_config = download_config,
        pinned_versions = pinned_versions,
        **kwargs
    )
