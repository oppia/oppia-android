"""
Rules that provide support for downloading configurations related to Firebase (i.e. the
google-services.json file).
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")
load("@bazel_skylib//rules:copy_file.bzl", "copy_file")

def _download_google_services_json_impl(ctx):
    output_file = ctx.outputs.output_file
    project_id = ctx.attr.project_id_flag[BuildSettingInfo].value
    app_id = ctx.attr.app_id_flag[BuildSettingInfo].value

    # Enforce that the Firebase App ID and Project ID are provided at execution time.
    if not app_id or not project_id:
        fail_command = """
        echo "ERROR: Need Firebase app & project IDs for downloading prod google-services.json."
        echo ""
        echo "Please specify them in your build command:"
        echo "  --//config:firebase_app_id=<your_firebase_app_id>"
        echo "  --//config:firebase_project_id=<your_firebase_project_id>"
        exit 1
        """
        ctx.actions.run_shell(
            outputs = [output_file],
            command = fail_command,
            mnemonic = "FailMissingFirebaseConfig",
            progress_message = "Failing due to missing Firebase configuration",
        )
        return DefaultInfo(files = depset([output_file]))

    command = """
    firebase apps:sdkconfig ANDROID "{app_id}" --project "{project_id}" --out "{output_file}" --non-interactive || exit 255
    """.format(
        app_id = app_id,
        project_id = project_id,
        output_file = output_file.path,
    )

    ctx.actions.run_shell(
        outputs = [output_file],
        inputs = [],
        tools = [],
        command = command,
        use_default_shell_env = True,
        mnemonic = "DownloadGoogleServicesJson",
        progress_message = "Downloading google-services.json from Firebase",
        execution_requirements = {
            "local": "1",  # Ensure the local 'firebase' command can be accessed.
            "requires-network": "1",  # This build step cannot run without internet connectivity.
        },
    )
    return DefaultInfo(files = depset([output_file]))

_download_google_services_json = rule(
    attrs = {
        "app_id_flag": attr.label(
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "project_id_flag": attr.label(
            mandatory = True,
        ),
    },
    implementation = _download_google_services_json_impl,
)

def prepare_google_services_json(name, developer_google_services_file, output_file):
    """
    Prepares a google_services_config.json file for use in google_services_xml() setups.

    The developer-only file will be provided unless //config:download_firebase_config is set to true
    which in turn requires both //config:firebase_project_id and //config:firebase_app_id to be
    provided and valid.

    Args:
        name: str. A unique name for this target.
        developer_google_services_file: label. The developer configuration file to fall back to.
        output_file: str. The name of the configuration output file that should be produced.
    """
    prod_file_name = "_%s_production_google_services.json" % name
    _download_google_services_json(
        name = "%s_download_production_google_services" % name,
        project_id_flag = "//config:firebase_project_id",
        app_id_flag = "//config:firebase_app_id",
        output_file = prod_file_name,
        tags = ["manual"],  # To ensure //... doesn't trigger a failure for an unconfigured environment.
    )

    copy_file(
        name = name,
        src = select({
            "//config:download_firebase_config_enabled": prod_file_name,
            "//conditions:default": developer_google_services_file,
        }),
        out = output_file,
    )
