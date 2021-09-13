"""
Macros & definitions corresponding to Oppia binary build flavors.
"""

load("//:oppia_android_application.bzl", "declare_deployable_application", "oppia_android_application")
load("//:version.bzl", "MAJOR_VERSION", "MINOR_VERSION", "VERSION_CODE")

# Defines the list of flavors available to build the Oppia app in. Note to developers: this list
# should be ordered by the development pipeline (i.e. features go through dev first, then other
# flavors as they mature).
AVAILABLE_FLAVORS = [
    "dev",
    "alpha",
]

# Note to developers: keys of this dict should follow the order of AVAILABLE_FLAVORS.
_FLAVOR_METADATA = {
    "dev": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 19,
        "target_sdk_version": 29,
        "multidex": "native",  # Legacy multidex not needed for dev builds.
        "proguard_specs": [],  # Developer builds are not optimized.
        "deps": [
            "//app",
        ],
    },
    "alpha": {
        "manifest": "//app:src/main/AndroidManifest.xml",
        "min_sdk_version": 19,
        "target_sdk_version": 29,
        "multidex": "legacy",
        "proguard_specs": [
            "config/proguard/androidx-proguard-rules.pro",
            "config/proguard/firebase-components-proguard-rules.pro",
            "config/proguard/glide-proguard-rules.pro",
            "config/proguard/google-play-services-proguard-rules.pro",
            "config/proguard/guava-proguard-rules.pro",
            "config/proguard/kotlin-proguard-rules.pro",
            "config/proguard/kotlinpoet-javapoet-proguard-rules.pro",
            "config/proguard/material-proguard-rules.pro",
            "config/proguard/moshi-proguard-rules.pro",
            "config/proguard/okhttp-proguard-rules.pro",
            "config/proguard/oppia-prod-proguard-rules.pro",
            "config/proguard/protobuf-proguard-rules.pro",
        ],
        "deps": [
            "//app",
        ],
    },
}

def _transform_android_manifest_impl(ctx):
    input_file = ctx.attr.input_file.files.to_list()[0]
    output_file = ctx.outputs.output_file
    git_meta_dir = ctx.attr.git_meta_dir.files.to_list()[0]
    build_flavor = ctx.attr.build_flavor
    major_version = ctx.attr.major_version
    minor_version = ctx.attr.minor_version
    version_code = ctx.attr.version_code

    # See corresponding transformation script for details on the passed arguments.
    arguments = [
        ".",  # Working directory of the Bazel repository.
        input_file.path,  # Path to the source manifest.
        output_file.path,  # Path to the output manifest.
        build_flavor,
        "%s" % major_version,
        "%s" % minor_version,
        "%s" % version_code,
        "origin/develop",  # The base branch for computing the version name.
    ]

    # Reference: https://docs.bazel.build/versions/master/skylark/lib/actions.html#run.
    ctx.actions.run(
        outputs = [output_file],
        inputs = ctx.files.input_file + [git_meta_dir],
        tools = [ctx.executable._transform_android_manifest_tool],
        executable = ctx.executable._transform_android_manifest_tool.path,
        arguments = arguments,
        mnemonic = "TransformAndroidManifest",
        progress_message = "Transforming Android manifest",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

_transform_android_manifest = rule(
    attrs = {
        "input_file": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "output_file": attr.output(
            mandatory = True,
        ),
        "git_meta_dir": attr.label(
            allow_files = True,
            mandatory = True,
        ),
        "build_flavor": attr.string(mandatory = True),
        "major_version": attr.int(mandatory = True),
        "minor_version": attr.int(mandatory = True),
        "version_code": attr.int(mandatory = True),
        "_transform_android_manifest_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:transform_android_manifest",
        ),
    },
    implementation = _transform_android_manifest_impl,
)

def define_oppia_binary_flavor(flavor):
    """
    Defines a new flavor of the Oppia Android app.

    Flavors are defined through properties defined within _FLAVOR_METADATA.

    This will define two targets:
    - //:oppia_<flavor> (the AAB)
    - //:install_oppia_<flavor> (the installable binary target--see declare_deployable_application
      for details)

    Args:
        flavor: str. The name of the flavor of the app. Must correspond to an entry in
            AVAILABLE_FLAVORS.
    """
    _transform_android_manifest(
        name = "oppia_%s_transformed_manifest" % flavor,
        input_file = _FLAVOR_METADATA[flavor]["manifest"],
        output_file = "AndroidManifest_transformed_%s.xml" % flavor,
        git_meta_dir = "//:.git",
        build_flavor = flavor,
        major_version = MAJOR_VERSION,
        minor_version = MINOR_VERSION,
        version_code = VERSION_CODE,
    )
    oppia_android_application(
        name = "oppia_%s" % flavor,
        custom_package = "org.oppia.android",
        enable_data_binding = True,
        config_file = "//:bundle_config.pb.json",
        manifest = ":AndroidManifest_transformed_%s.xml" % flavor,
        manifest_values = {
            "applicationId": "org.oppia.android",
            "minSdkVersion": "%d" % _FLAVOR_METADATA[flavor]["min_sdk_version"],
            "targetSdkVersion": "%d" % _FLAVOR_METADATA[flavor]["target_sdk_version"],
        },
        multidex = _FLAVOR_METADATA[flavor]["multidex"],
        proguard_generate_mapping = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        proguard_specs = _FLAVOR_METADATA[flavor]["proguard_specs"],
        shrink_resources = True if len(_FLAVOR_METADATA[flavor]["proguard_specs"]) != 0 else False,
        deps = _FLAVOR_METADATA[flavor]["deps"],
    )
    declare_deployable_application(
        name = "install_oppia_%s" % flavor,
        aab_target = ":oppia_%s" % flavor,
    )
