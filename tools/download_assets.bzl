"""
Rules for updating pinned lesson versions and downloading production assets for embedding directly
within app binary builds.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")
load("@bazel_skylib//rules:write_file.bzl", "write_file")
load("//:build_vars.bzl", "BUILD_SDK_VERSION")

_COMMON_ATTRS = {
    "base_url": attr.string(default = "https://www.oppia.org"),
    "download_config": attr.label(
        allow_single_file = True,
        mandatory = True,
    ),
    "gcs_base_url": attr.string(default = "https://storage.googleapis.com"),
    "gcs_bucket": attr.string(default = "oppiaserver-resources"),
    "pinned_versions": attr.label(
        allow_single_file = True,
        mandatory = True,
    ),
    "web_api_key_file": attr.label(
        default = Label("//config:web_api_key_file"),
    ),
}

def _download_prod_assets_impl(ctx):
    output_dir = ctx.actions.declare_directory(ctx.attr.output_dir_name)
    log_file = ctx.outputs.log
    api_key_path = ctx.attr.web_api_key_file[BuildSettingInfo].value

    if not api_key_path:
        fail_command = """
        echo "ERROR: Web API key is required for downloading prod/alpha assets."
        echo ""
        echo "Please specify it in your build command:"
        echo "  --//config:web_api_key_file=/path/to/your/api_key.txt"
        exit 1
        """
        ctx.actions.run_shell(
            outputs = [output_dir, log_file],
            command = fail_command,
            mnemonic = "FailMissingApiKey",
            progress_message = "Failing due to missing web API key",
        )
        return [DefaultInfo(files = depset([output_dir]))]

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

    script_content = """#!/bin/bash
set -e
TOOL_PATH="$1"
FINAL_LOG="$2"
shift 2

TMP_OUT="{tmp_out}"
FINAL_OUT="{final_out}"
LOG_FILE=$(mktemp /tmp/oppia_download_assets.XXXXXX.log)

cleanup() {{
  rm -rf "$TMP_OUT"
}}
trap cleanup EXIT

rm -rf "$TMP_OUT"
mkdir -p "$TMP_OUT"

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

mkdir -p "$FINAL_OUT"

if [ -d "$TMP_OUT/protov1/binary" ] && [ "$(ls -A "$TMP_OUT/protov1/binary")" ]; then
  cp "$TMP_OUT"/protov1/binary/*.pb "$FINAL_OUT"/
fi

if [ -d "$TMP_OUT/images" ] && [ "$(ls -A "$TMP_OUT/images")" ]; then
  mkdir -p "$FINAL_OUT/images"
  cp -r "$TMP_OUT"/images/* "$FINAL_OUT"/images/
fi

cp "$LOG_FILE" "$FINAL_LOG"
rm -f "$LOG_FILE"
""".format(
        tmp_out = tmp_out,
        final_out = output_dir.path,
    )

    ctx.actions.run_shell(
        outputs = [output_dir, log_file],
        inputs = inputs,
        tools = [ctx.executable._download_tool],
        command = script_content,
        arguments = [ctx.executable._download_tool.path, log_file.path] + tool_arguments,
        mnemonic = "DownloadProdAssets",
        progress_message = "Downloading/filtering/prepping prod assets",
        execution_requirements = {
            "requires-network": "1",  # This build step cannot run without internet connectivity.
        },
    )

    return [
        DefaultInfo(files = depset([output_dir])),
    ]

_download_prod_assets = rule(
    implementation = _download_prod_assets_impl,
    attrs = dict({
        "download_questions": attr.bool(mandatory = True),
        "output_dir_name": attr.string(mandatory = True),
        "output_log_name": attr.string(mandatory = True),
        "_download_tool": attr.label(
            default = Label("@oppia_android_asset_pipeline//scripts:download_lessons"),
            executable = True,
            cfg = "exec",
        ),
    }, **_COMMON_ATTRS),
    outputs = {
        "log": "%{output_log_name}",
    },
)

def downloaded_assets_library(name, download_config, pinned_versions, output_log_name, download_questions = False, tags = [], visibility = []):
    """
    Creates an android_library that packages remotely downloaded lessons as assets.

    This creates a library that contains all of the assets remotely downloaded from the Oppia web
    backend for the purpose of embedding the assets directly into built app binaries. It can only be
    used if the corresponding configuration parameters are correctly specified for the current app
    build, e.g. //config:assets_type.

    Args:
        name: str. The name of for the library being defined.
        download_config: label. The label to the download configuration textproto (which specifies
            exemptions to make during lesson download validation).
        pinned_versions: label. The label to the textproto containing all of the lesson versions
            that should be downloaded (which help provide determinism in the download process).
        output_log_name: str. The name of the produced output log file which will contain the full
            output of the download script including potential instructions for release maintainers.
        download_questions: bool. Whether to also download and include questions as part of the
            downloaded lesson bundle.
        tags: list of str. Tags that should be associated with the asset library.
        visibility: list of label. The visibilities that should be used for the asset library.
    """

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
            '    <uses-sdk android:minSdkVersion="23" android:targetSdkVersion="%d" />' % BUILD_SDK_VERSION,
            "</manifest>",
        ],
        tags = tags,
    )

    _download_prod_assets(
        name = downloaded_lessons_target,
        output_dir_name = asset_dir_name,
        output_log_name = output_log_name,
        download_config = download_config,
        pinned_versions = pinned_versions,
        download_questions = download_questions,
        tags = tags,
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
    api_key_path = ctx.attr.web_api_key_file[BuildSettingInfo].value
    script = ctx.actions.declare_file(ctx.label.name + ".sh")

    if not api_key_path:
        script_content = """#!/bin/bash
echo "ERROR: Web API key is required for running update_pinned_lesson_versions."
echo ""
echo "Please specify it in your build command:"
echo "  --//config:web_api_key_file=/path/to/your/api_key.txt"
exit 1
"""
    else:
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

"$TOOL" "{base_url}" "{gcs_base_url}" "{gcs_bucket}" "{api_key_path}" "$OUTPUT" "$CONFIG"

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
    """
    Creates a runnable target for regenerating a pinned version list.

    This creates a 'bazel run'-able target that, when run, will regenerate the specified pinned
    version list by downloading the latest list of compatible lesson versions from the Oppia web
    backend. This can only be used if the corresponding configuration parameters are correctly
    specified for the bazel run, e.g. //config:assets_type.

    Args:
        name: str. The name of for the library being defined.
        download_config: label. The label to the download configuration textproto (which specifies
            exemptions to make during lesson download validation).
        pinned_versions: label. The label to the textproto containing all of the lesson versions
            that should be downloaded (which help provide determinism in the download process).
        **kwargs: additional generic arguments such as tags and visibility.
    """

    _update_pinned_lesson_versions(
        name = name,
        download_config = download_config,
        pinned_versions = pinned_versions,
        **kwargs
    )
