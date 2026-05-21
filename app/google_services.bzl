"""
Macro and rule corresponding to production Firebase google-services.json downloads.
"""

load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")

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
        env = {},  # Inherits active local firebase developer login session!
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
    },
    outputs = {
        "output_file": "google-services-production.json",
    },
    implementation = _download_google_services_json_impl,
)

def download_google_services_json(name):
    """
    Downloads the production google-services.json file from Firebase using local CLI login credentials.

    Args:
        name: str. A unique name for this target.
    """
    _download_google_services_json(
        name = name,
        project_id_flag = "//config:firebase_project_id",
        app_id_flag = "//config:firebase_app_id",
    )
