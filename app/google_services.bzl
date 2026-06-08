"""
Macro and rule corresponding to production Firebase google-services.json downloads.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")
load("@bazel_skylib//rules:copy_file.bzl", "copy_file")

def _download_google_services_json_impl(ctx):
    output_file = ctx.outputs.output_file
    project_id = ctx.attr.project_id_flag[BuildSettingInfo].value
    app_id = ctx.attr.app_id_flag[BuildSettingInfo].value

    # Enforce that the Firebase App ID is provided.
    if not app_id:
        fail("\n\nERROR: Firebase App ID is required for downloading production google-services.json.\n" +
             "Please specify it in the build command using:\n" +
             "  --//config:firebase_app_id=<your_firebase_app_id>\n\n")

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
            "requires-network": "",
            "no-sandbox": "",  # Bypasses filesystem isolation to read active host login session!
        },
    )
    return DefaultInfo(files = depset([output_file]))

_download_google_services_json = rule(
    attrs = {
        "project_id_flag": attr.label(
            mandatory = True,
        ),
        "app_id_flag": attr.label(
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
    },
    implementation = _download_google_services_json_impl,
)

def prepare_google_services_json(name, developer_google_services_file, output_file):
    """
    Prepares a google_services_config.json file for use in google_services_xml() setups.

    If configured, this will remotely download the production configuration file.

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
        out = output_file
    )
